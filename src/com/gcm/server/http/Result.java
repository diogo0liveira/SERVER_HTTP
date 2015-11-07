package com.gcm.server.http;

import java.io.Serializable;

/**
 * Resultado de um request mensagem GCM que retornou código de status HTTP 200
 *
 * <p>
 * Se a mensagem é criada com êxito, o {@link #getMessageId()} retorna o id de mensagem e
 * {@link #getErrorCodeName()} retorna {@literal null}; caso contrário, {@link #getMessageId()} retorna
 * {@literal null} e {@link #getErrorCodeName()} retorna o código do erro.
 *
 * <p>
 * Há casos em que um request é aceite ea mensagem criada com êxito, mas GCM tem uma canonical    registration
 * id para esse dispositivo. Neste caso, o servidor deverá atualizar o registration id para evitar
 * solicitações rejeitadas no futuro.
 *
 * <p>
 * Em poucas palavras, o fluxo de trabalho para lidar com um resultado é:
 * <pre>
 *   - Chamar {@link #getMessageId()}:
 *   - {@literal null} quer dizer erro, chamada {@link #getErrorCodeName()}
 *   - non-{@literal null} significa que a mensagem foi criada:
 *   - Chamar {@link #getCanonicalRegistrationId()}
 *   - se ele retorna {@literal null}, não fazer nada.
 *   - caso contrário, atualizar o armazenamento de dados do servidor com o novo id.
 * </pre>
 */
public final class Result implements Serializable
{

    private final String messageId;
    private final String canonicalRegistrationId;
    private final String errorCode;

    public static final class Builder
    {

        // parametros opcionais
        private String messageId;
        private String canonicalRegistrationId;
        private String errorCode;

        public Builder canonicalRegistrationId(String value)
        {
            canonicalRegistrationId = value;
            return this;
        }

        public Builder messageId(String value)
        {
            messageId = value;
            return this;
        }

        public Builder errorCode(String value)
        {
            errorCode = value;
            return this;
        }

        public Result build()
        {
            return new Result(this);
        }
    }

    private Result(Builder builder)
    {
        canonicalRegistrationId = builder.canonicalRegistrationId;
        messageId = builder.messageId;
        errorCode = builder.errorCode;
    }

    /**
     * Obtém o message id, se houver.
     * @return 
     */
    public String getMessageId()
    {
        return messageId;
    }

    /**
     * Obtém o canonical registration id, se houver.
     * 
     * @return canonicalRegistrationId.
     */
    public String getCanonicalRegistrationId()
    {
        return canonicalRegistrationId;
    }

    /**
     * Obtém o código de erro, se houver.
     * @return 
     */
    public String getErrorCodeName()
    {
        return errorCode;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("[");

        if(messageId != null)
        {
            builder.append(" messageId=").append(messageId);
        }
        if(canonicalRegistrationId != null)
        {
            builder.append(" canonicalRegistrationId=")
                    .append(canonicalRegistrationId);
        }
        if(errorCode != null)
        {
            builder.append(" errorCode=").append(errorCode);
        }

        return builder.append(" ]").toString();
    }
}
