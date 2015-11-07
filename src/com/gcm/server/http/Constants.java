package com.gcm.server.http;

/**
 * Constantes usados na comunicação de serviço GCM.
 */
public final class Constants
{
    /**
     * Endpoint para envio de mensagens.
     */
    public static final String GCM_SEND_ENDPOINT = "https://android.googleapis.com/gcm/send";

    /**
     * HTTP parametro para registration id.
     */
    public static final String PARAM_REGISTRATION_ID = "registration_id";

    /**
     * HTTP parametro para collapse key.
     */
    public static final String PARAM_COLLAPSE_KEY = "collapse_key";

    /**
     * HTTP parametro para atrasar a entrega da mensagem, se o dispositivo estiver inactivo.
     */
    public static final String PARAM_DELAY_WHILE_IDLE = "delay_while_idle";

    /**
     * HTTP parâmetro para dizer GCM para validar a mensagem sem realmente enviá-lo.
     */
    public static final String PARAM_DRY_RUN = "dry_run";

    /**
     * Parâmetro HTTP para o nome do pacote que pode ser usado para restringir a entrega de mensagens por
     * correspondência contra o nome do pacote usado para gerar o ID de registro.
     */
    public static final String PARAM_RESTRICTED_PACKAGE_NAME = "restricted_package_name";

    /**
     * Prefixo de parâmetro HTTP usado para passar valores-chave na carga da mensagem.
     */
    public static final String PARAM_PAYLOAD_PREFIX = "data.";

    /**
     * Parâmetro utilizado para definir a mensagem de tempo-de-vida.
     */
    public static final String PARAM_TIME_TO_LIVE = "time_to_live";

    /**
     * Parâmetro utilizado para definir a prioridade da mensagem.
     */
    public static final String PARAM_PRIORITY = "priority";

    /**
     * Valor usado para definir a prioridade mensagem ao normal.
     */
    public static final String MESSAGE_PRIORITY_NORMAL = "normal";

    /**
     * Valor usado para definir a prioridade de mensagem para alta.
     */
    public static final String MESSAGE_PRIORITY_HIGH = "high";

    /**
     * Muitas mensagens enviadas pelo remetente. Tente novamente depois de um tempo.
     */
    public static final String ERROR_QUOTA_EXCEEDED = "QuotaExceeded";

    /**
     * Muitas mensagens enviadas pelo remetente a um dispositivo específico. Tente novamente depois de um
     * request.
     */
    public static final String ERROR_DEVICE_QUOTA_EXCEEDED = "DeviceQuotaExceeded";

    /**
     * Faltando registration_id. Sender deve sempre adicionar o registration_id ao request.
     */
    public static final String ERROR_MISSING_REGISTRATION = "MissingRegistration";

    /**
     * Ruim registration_id. Sender deve remover este registration_id.
     */
    public static final String ERROR_INVALID_REGISTRATION = "InvalidRegistration";

    /**
     * O sender_id contida no registration_id não corresponde ao sender_id usado para registrar com os
     * servidores GCM.
     */
    public static final String ERROR_MISMATCH_SENDER_ID = "MismatchSenderId";

    /**
     * O usuário tem desinstalado a aplicação ou desligado notificações. Sender deve parar de enviar      
     * mensagens para este dispositivo e apagar o registration_id. O cliente precisa registrar novamente com o
     * GCM servidores para receber notificações de novo.
     */
    public static final String ERROR_NOT_REGISTERED = "NotRegistered";

    /**
     * A carga da mensagem é muito grande, ver as limitações. Reduzir o tamanho da mensagem.
     */
    public static final String ERROR_MESSAGE_TOO_BIG = "MessageTooBig";

    /**
     * Collapse key É necessário. Incluir collapse key no request.
     */
    public static final String ERROR_MISSING_COLLAPSE_KEY = "MissingCollapseKey";

    /**
     * Uma mensagem particular não poderia ser enviada porque os servidores GCM não estavam disponíveis. Usado
     * apenas em JSON pedidos, como em texto simples pedidos de indisponibilidade é indicado por uma response
     * 503.
     */
    public static final String ERROR_UNAVAILABLE = "Unavailable";

    /**
     * Uma mensagem particular não poderia ser enviada porque os servidores GCM encontrou um erro. Usado
     * apenas em JSON solicitações, como nos pedidos de texto simples erros internos são indicados por uma
     * response 500.
     */
    public static final String ERROR_INTERNAL_SERVER_ERROR = "InternalServerError";

    /**
     * Hora de valor transmitido ao vivo é menor que zero ou mais do que o máximo.
     */
    public static final String ERROR_INVALID_TTL = "InvalidTtl";

    /**
     * Token retornado pelo GCM quando uma mensagem foi enviada com sucesso.
     */
    public static final String TOKEN_MESSAGE_ID = "id";

    /**
     * Token retornado pelo GCM quando o ID de de registro solicitado tem um valor canonical.
     */
    public static final String TOKEN_CANONICAL_REG_ID = "registration_id";

    /**
     * Token retornado pelo GCM quando houve um erro ao enviar uma mensagem.
     */
    public static final String TOKEN_ERROR = "Error";

    /**
     * JSON campo somente representando os ids de registro.
     */
    public static final String JSON_REGISTRATION_IDS = "registration_ids";

    /**
     * JSON campo somente representando os dados de carga.
     */
    public static final String JSON_PAYLOAD = "data";

    /**
     * JSON campo somente que representa a carga notificação.
     */
    public static final String JSON_NOTIFICATION = "notification";

    /**
     * JSON campo representando o título de notificação.
     */
    public static final String JSON_NOTIFICATION_TITLE = "title";

    /**
     * JSON campo que representa o corpo de notificação.
     */
    public static final String JSON_NOTIFICATION_BODY = "body";

    /**
     * JSON campo representando o ícone de notificação.
     */
    public static final String JSON_NOTIFICATION_ICON = "icon";

    /**
     * JSON representando o campo de som de notificação.
     */
    public static final String JSON_NOTIFICATION_SOUND = "sound";

    /**
     * JSON representando o campo de badge de notificação.
     */
    public static final String JSON_NOTIFICATION_BADGE = "badge";

    /**
     * JSON representando o campo de tag de notificação.
     */
    public static final String JSON_NOTIFICATION_TAG = "tag";

    /**
     * JSON campo representando a cor de notificação.
     */
    public static final String JSON_NOTIFICATION_COLOR = "color";

    /**
     * JSON campo que representa click action da notificação.
     */
    public static final String JSON_NOTIFICATION_CLICK_ACTION = "click_action";

    /**
     * JSON campo que representa a chave de localização do corpo de notificação.
     */
    public static final String JSON_NOTIFICATION_BODY_LOC_KEY = "body_loc_key";

    /**
     * JSON campo representando os valores para a localização do corpo de notificação.
     */
    public static final String JSON_NOTIFICATION_BODY_LOC_ARGS = "body_loc_args";

    /**
     * JSON campo que representa a chave do título localização notificação.
     */
    public static final String JSON_NOTIFICATION_TITLE_LOC_KEY = "title_loc_key";

    /**
     * JSON representando os valores de campo de localização título notificação.
     */
    public static final String JSON_NOTIFICATION_TITLE_LOC_ARGS = "title_loc_args";

    /**
     * JSON campo que representa o número de mensagens de sucesso.
     */
    public static final String JSON_SUCCESS = "success";

    /**
     * JSON campo que representa o número de mensagens que falharam.
     */
    public static final String JSON_FAILURE = "failure";

    /**
     * JSON campo que representa o número de mensagens com um canonical registration id.
     */
    public static final String JSON_CANONICAL_IDS = "canonical_ids";

    /**
     * JSON campo que representa o id do request multicast.
     */
    public static final String JSON_MULTICAST_ID = "multicast_id";

    /**
     * JSON campo que representa o resultado de cada request individual.
     */
    public static final String JSON_RESULTS = "results";

    /**
     * JSON campo representando o campo de erro de uma requst individual.
     */
    public static final String JSON_ERROR = "error";

    /**
     * JSON campo enviado pelo GCM quando uma mensagem foi enviada com sucesso.
     */
    public static final String JSON_MESSAGE_ID = "message_id";

    /**
     * Retorno de erro quando registationId é invalido.
     */
    public static final String INVALID_REGISTRATION = "InvalidRegistration";

    private Constants()
    {
        throw new UnsupportedOperationException();
    }
}
