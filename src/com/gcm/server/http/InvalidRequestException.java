package com.gcm.server.http;

import java.io.IOException;

/**
 * Exceção lançada quando GCM retornou um erro devido a uma solicitação inválida.
 * <p>
 * Isso é equivalente a GCM mensagens que retornam um erro HTTP diferente de 200.
 */
public final class InvalidRequestException extends IOException
{
    private final int status;
    private final String description;

    public InvalidRequestException(int status)
    {
        this(status, null);
    }

    public InvalidRequestException(int status, String description)
    {
        super(getMessage(status, description));
        this.status = status;
        this.description = description;
    }

    private static String getMessage(int status, String description)
    {
        StringBuilder base = new StringBuilder("HTTP Status Código: ").append(status);

        if(description != null)
        {
            base.append("(").append(description).append(")");
        }

        return base.toString();
    }

    /**
     * Obtem o HTTP Status Code.
     *
     * @return código status.
     */
    public int getHttpStatusCode()
    {
        return status;
    }

    /**
     * Obtém a descrição do erro.
     *
     * @return descrição do erro.
     */
    public String getDescription()
    {
        return description;
    }
}
