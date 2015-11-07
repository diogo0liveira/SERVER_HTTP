package com.gcm.server.http;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GCM message.
 *
 * <p>
 * As instâncias dessa classe são imutáveis e deve ser criado usando um {@link Builder}. Examplo:
 *
 * <strong>Simples message:</strong>
 * <pre><code>
 * Message message = new Message.Builder().build();
 * </code></pre>
 *
 * <strong>Message com atributos opcionais:</strong>
 * <pre><code>
 * Message message = new Message.Builder()
 *    .collapseKey(collapseKey)
 *    .timeToLive(3)
 *    .delayWhileIdle(true)
 *    .dryRun(true)
 *    .restrictedPackageName(restrictedPackageName)
 *    .build();
 * </code></pre>
 *
 * <strong>Message com atributos opcionais e os dados de carga útil:</strong>
 * <pre><code>
 * Message message = new Message.Builder()
 *    .priority("normal")
 *    .collapseKey(collapseKey)
 *    .timeToLive(3)
 *    .delayWhileIdle(true)
 *    .dryRun(true)
 *    .restrictedPackageName(restrictedPackageName)
 *    .addData("key1", "value1")
 *    .addData("key2", "value2")
 *    .build();
 * </code></pre>
 */
public final class Message implements Serializable
{

    private final String collapseKey;
    private final Boolean delayWhileIdle;
    private final Integer timeToLive;
    private final Map<String, String> data;
    private final Boolean dryRun;
    private final String restrictedPackageName;
    private final String priority;
    private final Notification notification;

    /**
     * Prioridade em que a mensagem é enviada.
     */
    public enum Priority
    {
        NORMAL, HIGH
    }

    public static final class Builder
    {
        private final Map<String, String> data;

        // parametros opicionais
        private String collapseKey;
        private Boolean delayWhileIdle;
        private Integer timeToLive;
        private Boolean dryRun;
        private String restrictedPackageName;
        private String priority;
        private Notification notification;

        public Builder()
        {
            this.data = new LinkedHashMap<>();
        }

        /**
         * Define a propriedade collapseKey.
         *
         * Este parâmetro identifica um grupo de mensagens (por exemplo, com collapse_key: "Actualizações
         * disponível") que pode ser recolhido, de modo que apenas a última mensagem é enviada quando a
         * entrega pode ser retomado.
         *
         * @param value collapseKeuy
         *
         * @return Atual instance Message
         */
        public Builder collapseKeuy(String value)
        {
            collapseKey = value;
            return this;
        }

        /**
         * Define a propriedade delayWhileIdle (valor default é {@literal false}).
         *
         * @param value delayWhileIdle
         *
         * @return Atual instance Message
         */
        public Builder delayWhileIdle(boolean value)
        {
            delayWhileIdle = value;
            return this;
        }

        /**
         * Define quanto tempo (em segundos), a mensagem deve ser mantido em armazenamento GCM se o
         * dispositivo está offline. O tempo máximo de viver suportado é de 4 semanas.
         *
         * @param value timeToLive
         *
         * @return Atual instance Message
         */
        public Builder timeToLive(int value)
        {
            timeToLive = value;
            return this;
        }

        /**
         * Adiciona um par chave/valor para os dados de carga.
         *
         * @param key   chave
         * @param value valor
         *
         * @return Atual instance Message
         */
        public Builder addData(String key, String value)
        {
            data.put(key, value);
            return this;
        }

        /**
         * Define a propriedade DryRun (valor default é {@literal false}).
         *
         * Permite aos desenvolvedores testar um pedido sem enviar uma mensagem.
         *
         * @param value dryRun
         *
         * @return Atual instance Message
         */
        public Builder dryRun(boolean value)
        {
            dryRun = value;
            return this;
        }

        /**
         * Define a propriedade restrictedPackageName.
         *
         * Este parâmetro especifica o nome do pacote do aplicativo, onde os tokens de registro devem
         * corresponder, a fim de receber a mensagem.
         *
         * @param value restrictedPackageName
         * 
         * @return Atual instance Message
         */
        public Builder restrictedPackageName(String value)
        {
            restrictedPackageName = value;
            return this;
        }

        /**
         * Define a propriedade priority.
         *
         * Define a prioridade da mensagem. Os valores válidos são "normal" e "alto". No iOS, estas
         * correspondem a APNs prioridade 5 e 10.
         *
         * Por padrão, as mensagens são enviadas com prioridade normal. Prioridade normal otimiza o consumo de
         * bateria do aplicativo cliente, e deve ser usada quando é necessária a entrega imediata. Para
         * mensagens com prioridade normal, o aplicativo poderá receber a mensagem com atraso não
         * especificado. Quando uma mensagem é enviada com prioridade alta, ele é enviado imediatamente, eo
         * aplicativo pode acordar um dispositivo de dormir e abrir uma conexão de rede para o servidor.
         *
         * @param value priority
         * 
         * @return Atual instance Message
         */
        public Builder priority(Priority value)
        {
            switch(value)
            {
                case NORMAL:
                    priority = Constants.MESSAGE_PRIORITY_NORMAL;
                    break;
                case HIGH:
                    priority = Constants.MESSAGE_PRIORITY_HIGH;
                    break;
            }

            return this;
        }

        /**
         * Define a propriedade notification.
         *
         * Para obter mais informações sobre a mensagem de notificação e opções de mensagem de dados, @See
         * Payload.
         *
         * @param value notification
         * 
         * @return Atual instance Message
         */
        public Builder notification(Notification value)
        {
            notification = value;
            return this;
        }

        public Message build()
        {
            return new Message(this);
        }

    }

    private Message(Builder builder)
    {
        collapseKey = builder.collapseKey;
        delayWhileIdle = builder.delayWhileIdle;
        data = Collections.unmodifiableMap(builder.data);
        timeToLive = builder.timeToLive;
        dryRun = builder.dryRun;
        restrictedPackageName = builder.restrictedPackageName;
        priority = builder.priority;
        notification = builder.notification;
    }

    /**
     * Obtém collapse key.
     *
     * @return collapseKey
     */
    public String getCollapseKey()
    {
        return collapseKey;
    }

    /**
     * Obtém delayWhileIdle.
     *
     * @return delayWhileIdle
     */
    public Boolean isDelayWhileIdle()
    {
        return delayWhileIdle;
    }

    /**
     * Obtém TimeToLive (em segundos).
     *
     * @return timeToLive
     */
    public Integer getTimeToLive()
    {
        return timeToLive;
    }

    /**
     * Obtém dryRun.
     *
     * @return dryRun
     */
    public Boolean isDryRun()
    {
        return dryRun;
    }

    /**
     * Obtém RestrictedPackageName.
     *
     * @return restrictedPackageName
     */
    public String getRestrictedPackageName()
    {
        return restrictedPackageName;
    }

    /**
     * Obtém priority.
     *
     * @return priority
     */
    public String getPriority()
    {
        return priority;
    }

    /**
     * Obtém os dados carga, que é imutável.
     *
     * @return data
     */
    public Map<String, String> getData()
    {
        return data;
    }

    /**
     * Obtém carga notificação, o que é imutável.
     *
     * @return notification
     */
    public Notification getNotification()
    {
        return notification;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Message(");
        if(priority != null)
        {
            builder.append("priority=").append(priority).append(", ");
        }
        if(collapseKey != null)
        {
            builder.append("collapseKey=").append(collapseKey).append(", ");
        }
        if(timeToLive != null)
        {
            builder.append("timeToLive=").append(timeToLive).append(", ");
        }
        if(delayWhileIdle != null)
        {
            builder.append("delayWhileIdle=").append(delayWhileIdle).append(", ");
        }
        if(dryRun != null)
        {
            builder.append("dryRun=").append(dryRun).append(", ");
        }
        if(restrictedPackageName != null)
        {
            builder.append("restrictedPackageName=").append(restrictedPackageName).append(", ");
        }
        if(notification != null)
        {
            builder.append("notification: ").append(notification).append(", ");
        }

        if(!data.isEmpty())
        {
            builder.append("data: {");

            data.entrySet().stream().forEach((entry) ->
            {
                builder.append(entry.getKey()).append("=").append(entry.getValue())
                        .append(",");
            });

            builder.delete(builder.length() - 1, builder.length());
            builder.append("}");
        }

        if(builder.charAt(builder.length() - 1) == ' ')
        {
            builder.delete(builder.length() - 2, builder.length());
        }

        builder.append(")");
        return builder.toString();
    }
}
