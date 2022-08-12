package chat

import java.text.SimpleDateFormat
import java.util.*

class Chat(
    //Идентификатор чата
    val id: Int = 0,
    //Список пользователей-участников чата
    private val userList: List<ChatUser>,
    //Список сообщений чата
    private val msgList: MutableList<Message> = mutableListOf(),
    //количество "живых" сообщений в чате
    private var msgCount: Int = 0,
    //метка о том, что чат удалён
    var deleted: Boolean = false
) {
    override fun toString(): String {
        val users = userList.joinToString(separator = ",")
        return "id=$id, пользователи: $users"
    }

    fun getUserList(): List<ChatUser> {
        return userList
    }

    fun isUserInChat(user: ChatUser): Boolean {
        return user in userList
    }

    fun addMessage(message: Message) {
        msgList.add(message)
    }

    //возвращает все сообщения чата (включая удалённые)
    fun getMessageList(): MutableList<Message> {
        return msgList
    }

    //возвращает число НЕудалённых сообщений чата
    fun getMessageCount(): Int {
        return msgCount
    }

    fun changeMessageCount(value: Int) {
        msgCount += value
    }
}

class Message(
    //Идентификатор сообщения
    val id: Int = 0,
    //Идентификатор автора сообщения
    val author: ChatUser,
    //Идентификатор чата, в котором лежит сообщение
    val chat: Chat,
    //Время публикации сообщения в формате unixtime
    val date: Long = 0,
    //Время последнего редактирования сообщения в формате unixtime
    var editDate: Long = -1,
    //Текст сообщения
    var text: String,
    //Метка о том, что сообщение удалено
    var deleted: Boolean = false
) {
    override fun toString(): String {
        val datePattern = SimpleDateFormat("yyyy-MM-dd, HH:mm:ss")

        val edited = if (editDate != -1L) "(отредактировано ${
            datePattern.format(Date(editDate))
        })" else ""

        val written = datePattern.format(Date(date)) + edited
        val msgAuthor = author.getId()
        return "$id $written от $msgAuthor: $text"
    }
}