package ru.ogbozoyan.core.model

import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.OffsetDateTime

@Entity
@Table(name = "chat_history")
open class ChatHistory(
    @Id
    @Column(name = "message_id", nullable = false)
    open var messageId: Long? = null,

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    open var chat: Chat? = null,

    @NotNull
    @Column(name = "is_user", nullable = false)
    open var isUser: Boolean? = false,

    @NotNull
    @Column(name = "content", nullable = false, length = Integer.MAX_VALUE)
    open var content: String? = null,

    @Column(name = "content_type", nullable = false, length = Integer.MAX_VALUE)
    @Enumerated(EnumType.STRING)
    @ColumnDefault("PLAIN")
    open var contentType: ContentTypeEnum? = null,

    @ColumnDefault("now()")
    @Column(name = "created_at")
    open var createdAt: OffsetDateTime? = null,
)