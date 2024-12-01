package ru.ogbozoyan.core.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.ColumnDefault
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "chat")
open class Chat(
    @Id
    @ColumnDefault("uuid_generate_v4()")
    @Column(name = "chat_id", nullable = false)
    open var chatId: UUID? = null,

    @NotNull
    @Column(name = "user_id", nullable = false, length = Integer.MAX_VALUE)
    open var userId: String? = null,

    @NotNull
    @Column(name = "title", nullable = false, length = Integer.MAX_VALUE)
    open var title: String? = null,

    @ColumnDefault("now()")
    @Column(name = "created_at")
    open var createdAt: OffsetDateTime? = null
)