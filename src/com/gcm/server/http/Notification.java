package com.gcm.server.http;

import java.io.Serializable;
import java.util.*;

/**
 * GCM parte da mensagem de notificação.
 * <p>
 * <p>
 * As instâncias dessa classe são imutáveis e deve ser criado usando um {@link Builder}. Examplo:
 * <p>
 * <strong>Simples notification:</strong>
 * <pre><code>
 * Notification notification = new Notification.Builder("myicon").build();
 * </code></pre>
 * <p>
 * <strong>Notification com atributos opcionais:</strong>
 * <pre><code>
 * Notification notification = new Notification.Builder("myicon")
 *    .title("Hello world!")
 *    .body("Here is a more detailed description")
 *    .build();
 * </code></pre>
 */
public final class Notification implements Serializable
{
    private final String title;
    private final String body;
    private final String icon;
    private final String sound;
    private final Integer badge;
    private final String tag;
    private final String color;
    private final String clickAction;
    private final String bodyLocKey;
    private final List<String> bodyLocArgs;
    private final String titleLocKey;
    private final List<String> titleLocArgs;

    public static final class Builder
    {
        // parâmetros necessários
        private final String icon;

        // parâmetros opcionais
        private String title;
        private String body;
        private String sound;
        private Integer badge;
        private String tag;
        private String color;
        private String clickAction;
        private String bodyLocKey;
        private List<String> bodyLocArgs;
        private String titleLocKey;
        private List<String> titleLocArgs;

        public Builder(String icon)
        {
            this.icon = icon;
            this.sound = "default"; // o valor suportadas atualmente
        }

        /**
         * Define a propriedade título.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder title(String value)
        {
            title = value;
            return this;
        }

        /**
         * Define a propriedade body.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder body(String value)
        {
            body = value;
            return this;
        }

        /**
         * Define a propriedade som (valor default é {@literal default}).
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder sound(String value)
        {
            sound = value;
            return this;
        }

        /**
         * Define a propriedade badge.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder badge(int value)
        {
            badge = value;
            return this;
        }

        /**
         * Define a propriedade tag.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder tag(String value)
        {
            tag = value;
            return this;
        }

        /**
         * Define a propriedade cor no formato {@literal #rrggbb}.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder color(String value)
        {
            color = value;
            return this;
        }

        /**
         * Define a propriedade click action.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder clickAction(String value)
        {
            clickAction = value;
            return this;
        }

        /**
         * Define a propriedade chave localização do body.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder bodyLocKey(String value)
        {
            bodyLocKey = value;
            return this;
        }

        /**
         * Define o body valores localização da propriedade.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder bodyLocArgs(List<String> value)
        {
            bodyLocArgs = Collections.unmodifiableList(value);
            return this;
        }

        /**
         * Define a propriedade chave título localização.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder titleLocKey(String value)
        {
            titleLocKey = value;
            return this;
        }

        /**
         * Define a propriedade valores título de localização.
         *
         * @param value
         *
         * @return Atual instance Notification
         */
        public Builder titleLocArgs(List<String> value)
        {
            titleLocArgs = Collections.unmodifiableList(value);
            return this;
        }

        public Notification build()
        {
            return new Notification(this);
        }
    }

    private Notification(Builder builder)
    {
        title = builder.title;
        body = builder.body;
        icon = builder.icon;
        sound = builder.sound;
        badge = builder.badge;
        tag = builder.tag;
        color = builder.color;
        clickAction = builder.clickAction;
        bodyLocKey = builder.bodyLocKey;
        bodyLocArgs = builder.bodyLocArgs;
        titleLocKey = builder.titleLocKey;
        titleLocArgs = builder.titleLocArgs;
    }

    /**
     * Obtém o titulo.
     *
     * @return title
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Obtém o body.
     *
     * @return body
     */
    public String getBody()
    {
        return body;
    }

    /**
     * Obtém o icon.
     *
     * @return icon
     */
    public String getIcon()
    {
        return icon;
    }

    /**
     * Obtém o som.
     *
     * @return sound
     */
    public String getSound()
    {
        return sound;
    }

    /**
     * Obtém o badge.
     *
     * @return badge
     */
    public Integer getBadge()
    {
        return badge;
    }

    /**
     * Obtém a tag.
     *
     * @return tag
     */
    public String getTag()
    {
        return tag;
    }

    /**
     * Gets the color.
     *
     * @return color
     */
    public String getColor()
    {
        return color;
    }

    /**
     * Obtém o click action.
     *
     * @return clickAction
     */
    public String getClickAction()
    {
        return clickAction;
    }

    /**
     * Obtém o body localization key.
     *
     * @return bodyLocKey
     */
    public String getBodyLocKey()
    {
        return bodyLocKey;
    }

    /**
     * Obtém a lista valores de localização do body, que é imutável.
     *
     * @return bodyLocArgs (List)
     */
    public List<String> getBodyLocArgs()
    {
        return bodyLocArgs;
    }

    /**
     * Obtém a chave título localização.
     *
     * @return titleLocKey
     */
    public String getTitleLocKey()
    {
        return titleLocKey;
    }

    /**
     * Obtém a lista valores de localização do título, que é imutável.
     *
     * @return titleLocArgs (List)
     */
    public List<String> getTitleLocArgs()
    {
        return titleLocArgs;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder("Notification(");
        if(title != null)
        {
            builder.append("title=").append(title).append(", ");
        }
        if(body != null)
        {
            builder.append("body=").append(body).append(", ");
        }
        if(icon != null)
        {
            builder.append("icon=").append(icon).append(", ");
        }
        if(sound != null)
        {
            builder.append("sound=").append(sound).append(", ");
        }
        if(badge != null)
        {
            builder.append("badge=").append(badge).append(", ");
        }
        if(tag != null)
        {
            builder.append("tag=").append(tag).append(", ");
        }
        if(color != null)
        {
            builder.append("color=").append(color).append(", ");
        }
        if(clickAction != null)
        {
            builder.append("clickAction=").append(clickAction).append(", ");
        }
        if(bodyLocKey != null)
        {
            builder.append("bodyLocKey=").append(bodyLocKey).append(", ");
        }
        if(bodyLocArgs != null)
        {
            builder.append("bodyLocArgs=").append(bodyLocArgs).append(", ");
        }
        if(titleLocKey != null)
        {
            builder.append("titleLocKey=").append(titleLocKey).append(", ");
        }
        if(titleLocArgs != null)
        {
            builder.append("titleLocArgs=").append(titleLocArgs).append(", ");
        }
        if(builder.charAt(builder.length() - 1) == ' ')
        {
            builder.delete(builder.length() - 2, builder.length());
        }
        builder.append(")");
        return builder.toString();
    }
}
