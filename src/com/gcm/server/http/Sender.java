package com.gcm.server.http;

import static com.gcm.server.http.Constants.GCM_SEND_ENDPOINT;
import static com.gcm.server.http.Constants.JSON_CANONICAL_IDS;
import static com.gcm.server.http.Constants.JSON_ERROR;
import static com.gcm.server.http.Constants.JSON_FAILURE;
import static com.gcm.server.http.Constants.JSON_MESSAGE_ID;
import static com.gcm.server.http.Constants.JSON_MULTICAST_ID;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_BADGE;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_BODY;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_BODY_LOC_ARGS;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_BODY_LOC_KEY;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_CLICK_ACTION;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_COLOR;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_ICON;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_SOUND;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_TAG;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_TITLE;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_TITLE_LOC_ARGS;
import static com.gcm.server.http.Constants.JSON_NOTIFICATION_TITLE_LOC_KEY;
import static com.gcm.server.http.Constants.JSON_PAYLOAD;
import static com.gcm.server.http.Constants.JSON_REGISTRATION_IDS;
import static com.gcm.server.http.Constants.JSON_RESULTS;
import static com.gcm.server.http.Constants.JSON_SUCCESS;
import static com.gcm.server.http.Constants.PARAM_COLLAPSE_KEY;
import static com.gcm.server.http.Constants.PARAM_DELAY_WHILE_IDLE;
import static com.gcm.server.http.Constants.PARAM_DRY_RUN;
import static com.gcm.server.http.Constants.PARAM_PRIORITY;
import static com.gcm.server.http.Constants.PARAM_RESTRICTED_PACKAGE_NAME;
import static com.gcm.server.http.Constants.PARAM_TIME_TO_LIVE;
import static com.gcm.server.http.Constants.TOKEN_CANONICAL_REG_ID;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Classe auxiliar para enviar mensagens para o serviço GCM usando uma API Key.
 */
public class Sender
{
    protected static final String UTF8 = "UTF-8";

    /**
     * Atraso inicial antes da primeira repetição, sem jitter.
     */
    protected static final int BACKOFF_INITIAL_DELAY = 1000;
    /**
     * Atraso máximo antes de uma nova tentativa.
     */
    protected static final int MAX_BACKOFF_DELAY = 1024000;

    protected final Random random = new Random();
    protected static final Logger LOGGER = Logger.getLogger(Sender.class.getName());

    private final String key;

    /**
     * Default constructor.
     *
     * @param key API key obtido por meio do Console API Google.
     */
    public Sender(String key)
    {
        this.key = nonNull(key);
    }

    /**
     * Envia uma mensagem para um dispositivo, repetindo em caso de indisponibilidade.
     * <p>
     * <p>
     * <strong>Nota: </strong> este método utiliza exponencial back-off para repetir em caso de indisponibilidade do serviço e, portanto,
     * pode bloquear o segmento de chamada para muitos segundos.
     *
     * @param message        mensagem a ser enviada, incluindo o registration id do dispositivo.
     *
     * @param registrationId dispositivo em que a mensagem será enviada.
     *
     * @param retries        número de tentativas em caso de erros de indisponibilidade de serviço.
     *
     * @return resultado do pedido (ver sua javadoc para mais detalhes).
     *
     * @throws IllegalArgumentException se registrationId é {@literal null}.
     * @throws InvalidRequestException  se GCM não retornou um status 200 ou 5xx.
     * @throws IOException              se a mensagem não pôde ser enviada.
     */
    public Result send(Message message, String registrationId, int retries) throws IOException
    {
        int attempt = 0;
        Result result;
        int backoff = BACKOFF_INITIAL_DELAY;
        boolean tryAgain;

        do
        {
            attempt++;
            if(LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Tentativa #{0} para enviar mensagem {1} para regIds {2}", new Object[]
                {
                    attempt, message, registrationId
                });
            }
            result = sendNoRetry(message, registrationId);
            tryAgain = result == null && attempt <= retries;

            if(tryAgain)
            {
                int sleepTime = backoff / 2 + random.nextInt(backoff);
                sleep(sleepTime);

                if(2 * backoff < MAX_BACKOFF_DELAY)
                {
                    backoff *= 2;
                }
            }
        }
        while(tryAgain);

        if(result == null)
        {
            throw new IOException("Não foi possível enviar mensagem depois " + attempt + " tentativas");
        }
        return result;
    }

    /**
     * Envia uma mensagem sem repetir em caso de indisponibilidade do serviço. Consulte {@link #send(Message, String, int)} para mais
     * informações.
     *
     * @param message        mensagem a ser enviada.
     *
     * @param registrationId registration id do dispositivo que recebera a mensagem.
     *
     * @return resultado do post, ou {@literal null} se o serviço não estava disponível GCM ou qualquer exceção rede causado a solicitação
     *         para falhar.
     *
     * @throws InvalidRequestException  se GCM não retornou status 200.
     * @throws IllegalArgumentException se registrationId é {@literal null}.
     */
    public Result sendNoRetry(Message message, String registrationId) throws IOException
    {
        nonNull(registrationId);
        List<String> registrationIds = Collections.singletonList(registrationId);
        MulticastResult multicastResult = sendNoRetry(message, registrationIds);

        if(multicastResult == null)
        {
            return null;
        }

        List<Result> results = multicastResult.getResults();

        if(results.size() != 1)
        {
            LOGGER.log(Level.WARNING, "Encontrado {0} resultados em única solicitação multicast, esperado um.", results.size());
            return null;
        }

        return results.get(0);
    }

    /**
     * Envia uma mensagem para muitos dispositivos, repetindo em caso de indisponibilidade.
     * <p>
     * <p>
     * <strong>Nota: </strong> Este método utiliza exponencial back-off para repetir em caso de serviço indisponibilidade e, portanto, pode
     * bloquear o segmento de chamada por muitos segundos.
     *
     * @param message mensagem a ser enviada.
     * @param regIds  registration id dos dispositivos que receberão a mensagem.
     * @param retries número de tentativas em caso de erros de indisponibilidade de serviço.
     *
     * @return resultado combinado de todas as solicitações feitas.
     *
     * @throws IllegalArgumentException se registrationIds é {@literal null} ou empty.
     * @throws InvalidRequestException  se GCM não retornou status 200 ou 503.
     * @throws IOException              se a mensagem não pôde ser enviada.
     */
    public MulticastResult send(Message message, List<String> regIds, int retries) throws IOException
    {
        int attempt = 0;
        boolean tryAgain;
        MulticastResult multicastResult;
        int backoff = BACKOFF_INITIAL_DELAY;

        //Mapa de resultados por registration id, ele será atualizado depois de cada tentativa de enviar as mensagens
        Map<String, Result> results = new HashMap<>();
        List<String> unsentRegIds = new ArrayList<>(regIds);
        List<Long> multicastIds = new ArrayList<>();

        do
        {
            multicastResult = null;
            attempt++;

            if(LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Tentativa #{0} para enviar a mensagem {1} para regIds {2}", new Object[]
                {
                    attempt, message, unsentRegIds
                });
            }
            try
            {
                multicastResult = sendNoRetry(message, unsentRegIds);
            }
            catch(IOException e)
            {
                // sem necessidade de aviso desde já exceção pode ser registrada
                LOGGER.log(Level.FINEST, "IOException na tentativa " + attempt, e);
            }
            if(multicastResult != null)
            {
                long multicastId = multicastResult.getMulticastId();
                LOGGER.log(Level.FINE, "multicast_id na tentativa # {0}: {1}", new Object[]
                {
                    attempt, multicastId
                });

                multicastIds.add(multicastId);
                unsentRegIds = updateStatus(unsentRegIds, results, multicastResult);
                tryAgain = !unsentRegIds.isEmpty() && attempt <= retries;
            }
            else
            {
                tryAgain = attempt <= retries;
            }

            if(tryAgain)
            {
                int sleepTime = backoff / 2 + random.nextInt(backoff);
                sleep(sleepTime);

                if(2 * backoff < MAX_BACKOFF_DELAY)
                {
                    backoff *= 2;
                }
            }
        }
        while(tryAgain);

        if(multicastIds.isEmpty())
        {
            // todas as mensagens JSON falhou devido a indisponibilidade GCM
            throw new IOException("Não foi possível postar solicitações JSON para GCM depois de " + attempt + " tentativas");
        }

        // calcular resumo
        int success = 0, failure = 0, canonicalIds = 0;

        for(Result result : results.values())
        {
            if(result.getMessageId() != null)
            {
                success++;
                if(result.getCanonicalRegistrationId() != null)
                {
                    canonicalIds++;
                }
            }
            else
            {
                failure++;
            }
        }

        // construir um novo objeto com o resultado global
        long multicastId = multicastIds.remove(0);
        MulticastResult.Builder builder = new MulticastResult.Builder(success,
                failure, canonicalIds, multicastId).retryMulticastIds(multicastIds);

        // adicionar resultados, na mesma ordem que a entrada
        regIds.stream().map((regId) -> results.get(regId)).forEach((result)
                -> 
                {
                    builder.addResult(result);
        });

        return builder.build();
    }

    /**
     * Atualiza o status das mensagens enviadas para dispositivos ea lista de dispositivos que devem ser repetida.
     *
     * @param unsentRegIds    lista de dispositivos que ainda estão pendentes uma atualização.
     * @param allResults      map do estado que será atualizado.
     * @param multicastResult resultado do último envio multicast.
     *
     * @return versão atualizada de dispositivos que devem ser repetida.
     */
    private List<String> updateStatus(List<String> unsentRegIds, Map<String, Result> allResults, MulticastResult multicastResult)
    {
        List<Result> results = multicastResult.getResults();

        if(results.size() != unsentRegIds.size())
        {
            // nunca deveria acontecer, a menos que haja uma falha no algoritmo
            throw new RuntimeException("Erro interno: tamanhos não correspondem." + "currentResults: " + results + "; unsentRegIds: " + unsentRegIds);
        }

        List<String> newUnsentRegIds = new ArrayList<>();

        for(int i = 0; i < unsentRegIds.size(); i++)
        {
            String regId = unsentRegIds.get(i);
            Result result = results.get(i);
            allResults.put(regId, result);
            String error = result.getErrorCodeName();

            if(error != null && (error.equals(Constants.ERROR_UNAVAILABLE) || error.equals(Constants.ERROR_INTERNAL_SERVER_ERROR)))
            {
                newUnsentRegIds.add(regId);
            }
        }

        return newUnsentRegIds;
    }

    /**
     * Envia uma mensagem sem repetir em caso de indisponibilidade do serviço. Consulte {@link #send(Message, List, int)} para mais
     * informações.
     *
     * @param message         mensagem a ser enviada.
     * @param registrationIds registration id do dispositivo que recebera a mensagem.
     *
     * @return multicast resulta se a mensagem foi enviada com sucesso, {@literal null} se ele falhou, mas poderia ser repetida.
     *
     * @throws IllegalArgumentException se registrationIds é {@literal null} ou empty.
     * @throws InvalidRequestException  se GCM não retornar status 200.
     * @throws IOException              se houve um erro de parsing JSON.
     */
    public MulticastResult sendNoRetry(Message message, List<String> registrationIds) throws IOException
    {
        if(nonNull(registrationIds).isEmpty())
        {
            throw new IllegalArgumentException("registrationIds não pode estar vazio");
        }

        Map<Object, Object> jsonRequest = new HashMap<>();
        setJsonField(jsonRequest, PARAM_PRIORITY, message.getPriority());
        setJsonField(jsonRequest, PARAM_TIME_TO_LIVE, message.getTimeToLive());
        setJsonField(jsonRequest, PARAM_COLLAPSE_KEY, message.getCollapseKey());
        setJsonField(jsonRequest, PARAM_RESTRICTED_PACKAGE_NAME, message.getRestrictedPackageName());
        setJsonField(jsonRequest, PARAM_DELAY_WHILE_IDLE, message.isDelayWhileIdle());
        setJsonField(jsonRequest, PARAM_DRY_RUN, message.isDryRun());
        jsonRequest.put(JSON_REGISTRATION_IDS, registrationIds);
        Map<String, String> payload = message.getData();

        if(!payload.isEmpty())
        {
            jsonRequest.put(JSON_PAYLOAD, payload);
        }

        if(message.getNotification() != null)
        {
            Notification notification = message.getNotification();
            Map<Object, Object> nMap = new HashMap<>();

            if(notification.getBadge() != null)
            {
                setJsonField(nMap, JSON_NOTIFICATION_BADGE, notification.getBadge().toString());
            }
            setJsonField(nMap, JSON_NOTIFICATION_BODY, notification.getBody());
            setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_ARGS, notification.getBodyLocArgs());
            setJsonField(nMap, JSON_NOTIFICATION_BODY_LOC_KEY, notification.getBodyLocKey());
            setJsonField(nMap, JSON_NOTIFICATION_CLICK_ACTION, notification.getClickAction());
            setJsonField(nMap, JSON_NOTIFICATION_COLOR, notification.getColor());
            setJsonField(nMap, JSON_NOTIFICATION_ICON, notification.getIcon());
            setJsonField(nMap, JSON_NOTIFICATION_SOUND, notification.getSound());
            setJsonField(nMap, JSON_NOTIFICATION_TAG, notification.getTag());
            setJsonField(nMap, JSON_NOTIFICATION_TITLE, notification.getTitle());
            setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_ARGS, notification.getTitleLocArgs());
            setJsonField(nMap, JSON_NOTIFICATION_TITLE_LOC_KEY, notification.getTitleLocKey());
            jsonRequest.put(JSON_NOTIFICATION, nMap);
        }

        String requestBody = JSONValue.toJSONString(jsonRequest);
        LOGGER.log(Level.FINEST, "JSON request: {0}", requestBody);
        HttpURLConnection conn;
        int status;

        try
        {
            conn = post(GCM_SEND_ENDPOINT, "application/json", requestBody);
            status = conn.getResponseCode();
        }
        catch(IOException e)
        {
            LOGGER.log(Level.FINE, "IOException postagem para GCM", e);
            return null;
        }

        String responseBody;

        if(status != 200)
        {
            try
            {
                responseBody = getAndClose(conn.getErrorStream());
                LOGGER.log(Level.FINEST, "JSON error response: {0}", responseBody);
            }
            catch(IOException e)
            {
                // ignorar a exceção, uma vez que será lançada uma InvalidRequestException de qualquer maneira
                responseBody = "N/A";
                LOGGER.log(Level.FINE, "Exception reading response: ", e);
            }

            throw new InvalidRequestException(status, responseBody);
        }
        try
        {
            responseBody = getAndClose(conn.getInputStream());
        }
        catch(IOException e)
        {
            LOGGER.log(Level.WARNING, "IOException reading response", e);
            return null;
        }

        LOGGER.log(Level.FINEST, "JSON response: {0}", responseBody);
        JSONParser parser = new JSONParser();
        JSONObject jsonResponse;

        try
        {
            jsonResponse = (JSONObject)parser.parse(responseBody);
            int success = getNumber(jsonResponse, JSON_SUCCESS).intValue();
            int failure = getNumber(jsonResponse, JSON_FAILURE).intValue();
            int canonicalIds = getNumber(jsonResponse, JSON_CANONICAL_IDS).intValue();
            long multicastId = getNumber(jsonResponse, JSON_MULTICAST_ID).longValue();
            MulticastResult.Builder builder = new MulticastResult.Builder(success, failure, canonicalIds, multicastId);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>)jsonResponse.get(JSON_RESULTS);

            if(results != null)
            {
                results.stream().map((jsonResult)
                        -> 
                        {
                            String messageId = (String)jsonResult.get(JSON_MESSAGE_ID);
                            String canonicalRegId = (String)jsonResult.get(TOKEN_CANONICAL_REG_ID);
                            String error = (String)jsonResult.get(JSON_ERROR);

                            Result result = new Result.Builder()
                                    .messageId(messageId)
                                    .canonicalRegistrationId(canonicalRegId)
                                    .errorCode(error)
                                    .build();
                            return result;

                }).forEach((result)
                        -> 
                        {
                            builder.addResult(result);
                });
            }
            return builder.build();
        }
        catch(ParseException | CustomParserException e)
        {
            throw newIoException(responseBody, e);
        }
    }

    private IOException newIoException(String responseBody, Exception e)
    {
        // log exceção, como construtor IOException que leva uma mensagem e causa só está disponível em Java 6
        String msg = "Error parsing JSON response (" + responseBody + ")";
        LOGGER.log(Level.WARNING, msg, e);
        return new IOException(msg + ":" + e);
    }

    private static void close(Closeable closeable)
    {
        if(closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch(IOException e)
            {
                // ignorar erro
                LOGGER.log(Level.FINEST, "IOException closing stream", e);
            }
        }
    }

    /**
     * Define um campo JSON, mas apenas se o valor não for {@literal null}.
     */
    private void setJsonField(Map<Object, Object> json, String field, Object value)
    {
        if(value != null)
        {
            json.put(field, value);
        }
    }

    private Number getNumber(Map<?, ?> json, String field)
    {
        Object value = json.get(field);

        if(value == null)
        {
            throw new CustomParserException("Campo faltando: " + field);
        }

        if(!(value instanceof Number))
        {
            throw new CustomParserException("Campo " + field + " não contém um número: " + value);
        }

        return (Number)value;
    }

    class CustomParserException extends RuntimeException
    {
        CustomParserException(String message)
        {
            super(message);
        }
    }

    /**
     * Faça um POST de HTTP a uma determinada URL.
     *
     * @param url  URL para o POST.
     *
     * @param body corpo do POST a ser enviado.
     *
     * @return HTTP response.
     *
     * @throws IOException propagadas a partir de métodos subjacentes.
     */
    protected HttpURLConnection post(String url, String body) throws IOException
    {
        return post(url, "application/x-www-form-urlencoded;charset=UTF-8", body);
    }

    /**
     * Faz uma solicitação POST HTTP a um endpoint.
     * <p>
     * <p>
     * <strong>Nota: </strong> o retorno conectado não pode deve ser desligado, caso contrário ele iria matar conexões persistentes feitas
     * usando Keep-Alive.
     *
     * @param url         endpoint POST post e request.
     * @param contentType tipo do request.
     * @param body        corpo do request.
     *
     * @return the underlying connection.
     *
     * @throws IOException propagadas a partir de métodos subjacentes.
     */
    protected HttpURLConnection post(String url, String contentType, String body) throws IOException
    {
        if(url == null || contentType == null || body == null)
        {
            throw new IllegalArgumentException("Estes argumentos não podem ser nulos");
        }

        if(!url.startsWith("https://"))
        {
            LOGGER.log(Level.WARNING, "URL não utiliza https: {0}", url);
        }

        LOGGER.log(Level.FINE, "Enviando POST to {0}", url);
        LOGGER.log(Level.FINEST, "POST body: {0}", body);

        byte[] bytes = body.getBytes(UTF8);
        HttpURLConnection conn = getConnection(url);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        conn.setFixedLengthStreamingMode(bytes.length);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", contentType);
        conn.setRequestProperty("Authorization", "key=" + key);
        OutputStream out = conn.getOutputStream();

        try
        {
            out.write(bytes);
        }
        finally
        {
            close(out);
        }
        return conn;
    }

    /**
     * Cria um map com apenas um par chave-valor.
     *
     * @param key   chave do item do map.
     *
     * @param value valor do item do map.
     *
     * @return
     */
    protected static final Map<String, String> newKeyValues(String key, String value)
    {
        Map<String, String> keyValues = new HashMap<>(1);
        keyValues.put(nonNull(key), nonNull(value));
        return keyValues;
    }

    /**
     * Cria um {@link StringBuilder} para ser utilizado como o body de um HTTP POST.
     *
     * @param name  parâmetro inicial para o POST.
     * @param value valor inicial para esse parâmetro.
     *
     * @return StringBuilder para ser utilizado um HTTP POST body.
     */
    protected static StringBuilder newBody(String name, String value)
    {
        return new StringBuilder(nonNull(name)).append('=').append(nonNull(value));
    }

    /**
     * Adds a new parameter to the HTTP POST body.
     *
     * @param body  HTTP POST body.
     * @param name  nome do parametro.
     * @param value valor do parametro.
     */
    protected static void addParameter(StringBuilder body, String name, String value)
    {
        nonNull(body).append('&').append(nonNull(name)).append('=').append(nonNull(value));
    }

    /**
     * Obtém um {@link HttpURLConnection} um dado URL.
     *
     * @param url URL para conexão.
     *
     * @return HttpURLConnection com a URL determinada.
     *
     * @throws IOException
     */
    protected HttpURLConnection getConnection(String url) throws IOException
    {
        return (HttpURLConnection)new URL(url).openConnection();
    }

    /**
     * Método de conveniência para converter um InputStream para um String.
     * <p>
     * Se o fluxo termina em um caractere de nova linha, que vai ser removido.
     * <p>
     * Se o stream é {@literal null}, retornar um string empty.
     *
     * @param stream InputStream a ser convertido.
     *
     * @return retorna a string tratada.
     *
     * @throws IOException
     */
    protected static String getString(InputStream stream) throws IOException
    {
        if(stream == null)
        {
            return "";
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder content = new StringBuilder();
        String newLine;

        do
        {
            newLine = reader.readLine();

            if(newLine != null)
            {
                content.append(newLine).append('\n');
            }
        }
        while(newLine != null);

        if(content.length() > 0)
        {
            // tira última de nova linha
            content.setLength(content.length() - 1);
        }

        return content.toString();
    }

    private static String getAndClose(InputStream stream) throws IOException
    {
        try
        {
            return getString(stream);
        }
        finally
        {
            if(stream != null)
            {
                close(stream);
            }
        }
    }

    static <T> T nonNull(T argument)
    {
        if(argument == null)
        {
            throw new IllegalArgumentException("argumento não pode ser nulo");
        }

        return argument;
    }

    void sleep(long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch(InterruptedException e)
        {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Verifica se o registation id enviado pelo cliente é valido.
     *
     * @param registrationId registration id do dispositivo que recebera a mensagem.
     *
     * @return retorna true se a resgitrationId é valido.
     *
     * @throws IOException se houve um erro de parsing JSON.
     */
    public boolean checkingRegistrationId(String registrationId) throws IOException
    {
        int attempt = 0;
        int backoff = BACKOFF_INITIAL_DELAY;

        boolean tryAgain;
        boolean sucess = false;

        Result result;
        Message message = new Message.Builder().dryRun(true).build();

        do
        {
            attempt++;
            if(LOGGER.isLoggable(Level.FINE))
            {
                LOGGER.log(Level.FINE, "Tentativa #{0} para enviar mensagem para regIds {1}", new Object[]
                {
                    attempt, registrationId
                });
            }

            result = sendNoRetry(message, registrationId);
            tryAgain = (result == null && attempt <= 5);

            if(tryAgain)
            {
                int sleepTime = ((backoff / 2) + random.nextInt(backoff));
                sleep(sleepTime);

                if(2 * backoff < MAX_BACKOFF_DELAY)
                {
                    backoff *= 2;
                }
            }
        }
        while(tryAgain);

        if(result == null)
        {
            throw new IOException("Não foi possível enviar mensagem depois " + attempt + " tentativas");
        }
        else
        {
            sucess = !((result.getErrorCodeName() != null) && result.getErrorCodeName().equals(Constants.INVALID_REGISTRATION));
        }

        return sucess;
    }
}
