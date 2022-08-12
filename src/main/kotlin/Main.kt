import chat.Chat
import chat.ChatService
import chat.ChatUser
import chat.Message

fun main() {
    val chat = ChatService()
    val u1 = chat.createUser()
    val u2 = chat.createUser()
    val u3 = chat.createUser()

    u1.newMessage(u2, "msg1_2_1")
    u1.newMessage(u2, "msg1_2_2")
    u1.newMessage(u3, "msg1_3_1")
    u1.newMessage(u3, "msg1_3_2")
    u2.newMessage(u3, "msg2_3_1")
    println("----чаты у u1----")
    printChats(u1)
    var c1 = u1.getChat(u2)!! //этот чат точно есть - мы его только что создали
    println("----сообщения в чате между u1 и u2----")
    printMessagesFromChat(u1, c1)
    var m1 = u1.getMessages(c1)!![0] //и сообщения в чате тоже точно есть
    println("----редактирование----")
    u1.editMessage(m1, "QQQ")
    printMessagesFromChat(u1, c1)
    println("----удаление сообщения----")
    u1.deleteMessage(c1, m1)
    printMessagesFromChat(u1, c1)
    println("----...вместе с чатом----")
    u1.deleteMessage(c1, m1)
    printChats(u1)
    println("----удаление чата отдельно----")
    u3.deleteChat(u3.getChat(u1)!!) //удаляем от лица другого пользователя
    printChats(u1)

    println("----создаём новые чаты с теми же пользователями----")
    u1.newMessage(u2, "msg1_2_1")
    u1.newMessage(u2, "msg1_2_2")
    u1.newMessage(u2, "msg1_2_3")
    u1.newMessage(u2, "msg1_2_4")
    u3.newMessage(u2, "msg3_2_1")
    printChats(u1)
    println("----проверяем непрочитанные сообщения u2----")
    printUnreadAll(u2)
    println("----удаляем через u1, проверяем ещё раз u2----")
    c1 = u1.getChat(u2)!!
    m1 = u1.getMessages(c1)!![0]
    var m2 = u1.getMessages(c1)!![1]
    u1.deleteMessage(c1, m1)
    printUnreadAll(u2)
    println("----пытаемся удалить чужое сообщение----")
    u2.deleteMessage(c1, m2)
    println("----...при удалении автоматически читаются непрочитанные сообщения----")
    printUnreadAll(u2)
    println("----пытаемся удалить чужое сообщение в чужом чате----")
    u3.deleteMessage(c1, m2)
    println("----пытаемся удалить чужой чат----")
    u3.deleteChat(c1)
    println("----при написании нового сообщения в чат - непрочитанные в нём так же читаются----")
    u2.newMessage(u1, "msg2_1_1")
    printUnreadAll(u1) //отобразит одно новое сообщение у u1
    u1.newMessage(u2, "msg1_2_3")
    printUnreadAll(u1) //не должно отображать новых сообщений у u1
    println("----получаем непрочитанные сообщения из конкретного чата----")
    var c2 = u2.getChat(u3)!!
    printUnreadInChat(u2, c2)
    println("----пытаемся получить сообщения из чужого чата----")
    printUnreadInChat(u2, c2)

    println("----получаем список сообщений из чата со стартового id с ограничением по количеству----")
    printMessagesFromChat(u1, c1, 0, 2)
    println("----получаем список последних сообщений из чата с ограничением по количеству----")
    printMessagesFromChat(u1, c1, -1, 2)

}

//печатаем из чата первые count неудалённых сообщений, начиная со startId
fun printMessagesFromChat(user: ChatUser, chat: Chat, startId: Int = 0, count: Int = 5): List<Message>? {
    val result = user.getMessages(chat, startId, count)
    if (result == null) {
        println("Указанный чат недоступен либо удалён")
        return null
    }
    result.forEach { println(it) }
    return result
}

//получаем список непрочитанных чатов с количеством непрочитанных в них сообщений
fun printUnreadAll(user: ChatUser): HashMap<Chat, Int> {
    val chatSvc = user.getChatService()
    val result = chatSvc.getUnreadTotal(user.getUnreadList())
    result.forEach { println("Не прочитано сообщений: ${it.value} в чате с id=${it.key.id}") }
    return result
}

fun printUnreadInChat(user: ChatUser, chat: Chat): List<Message> {
    val result = user.getUnreadList().filter { it.chat == chat }
    println("Не прочитано сообщений: ${result.size} в чате с id=${chat.id}")
    result.forEach { println(it) }
    return result
}

//выводим список чатов (кроме удалённых)
fun printChats(user: ChatUser) {
    val result = user.getChatsAll()
    println("Всего у пользователя id=${user.getId()} чатов ${result.size}:")
    result.forEach { println(it) }
}
