package chat

class ChatService {
    private val chatList = mutableListOf<Chat>()
    private val messageList = mutableListOf<Message>()
    private val userList = mutableListOf<ChatUser>()
    private var chatId = 0
    private var messageId = 0
    private var userId = 0

    fun createUser(): ChatUser {
        userId++
        val user = ChatUser(userId, chatService = this)
        userList.add(user)
        return user
    }

    fun addUserToChat(fromUser: ChatUser, toUser: ChatUser, chat: Chat) {
        toUser.addChat(fromUser, chat)
    }

    //создаём чат и возвращаем его экземпляр вызывающему
    fun createChat(users: List<ChatUser>): Chat? {
        val validUsers = checkUserValidity(users) ?: return null
        chatId++
        //создаём чат, присваиваем ему id, запихиваем список пользователей
        val chat = Chat(chatId, validUsers)
        chatList.add(chat)
        return chat
    }

    //проверяем годноту переданных нам id пользователей
    private fun checkUserValidity(users: List<ChatUser>): List<ChatUser>? {
        //distinct возвращает неповторяемые элементы
        val validUsers = users.distinct()
        if (validUsers.size > 1) return validUsers
        println("В чате должно быть 2 существующих пользователя")
        return null
    }

    //создаём сообщение в указанный чат
    fun newMessage(chat: Chat, author: ChatUser, text: String) {
        if (!chat.isUserInChat(author)) return
        messageId++
        val currentTime = System.currentTimeMillis()
        val message = Message(id = messageId, date = currentTime, author = author, chat = chat, text = text)
        messageList.add(message)
        //добавляем сообщение в список сообщений чата
        chat.addMessage(message)
        chat.changeMessageCount(1)
        //добавляем сообщение в список непрочитанных пользователю, которому написали
        val uList = chat.getUserList()
        val toUser = uList.first { it != author }
        toUser.addUnreadMessage(message)
    }

    //удаляем сообщение
    fun deleteMessage(message: Message, user: ChatUser) {
        if (!message.chat.isUserInChat(user)) return
        if (message.author != user) {
            println("Удалить чужое сообщение нельзя")
            return
        }
        //помечаем сообщение удалённым
        message.deleted = true
        message.chat.changeMessageCount(-1)
        //если это было последнее сообщение - чат тоже помечаем удаленным
        if (message.chat.getMessageCount() == 0) deleteChat(message.chat, user)
    }

    //редактируем указанное сообщение
    fun editMessage(author: ChatUser, message: Message, text: String) {
        if (message.author != author) {
            println("Нельзя редактировать чужое сообщение")
            return
        }
        val currentTime = System.currentTimeMillis()
        message.text = text
        message.editDate = currentTime
    }

    //возвращаем запрошенные сообщения из чата
    fun getMessageList(
        chat: Chat,
        user: ChatUser,
        startId: Int = -1,
        count: Int = 5
    ): List<Message>? {
        //если пользователь не в чате - ничего ему не отдаём
        if (!chat.isUserInChat(user)) return null
        //если указан номер сообщения - передаём количество, начиная с указанного id
        return if (startId > -1) {
            chat.getMessageList().asSequence()
                .filter { !it.deleted && it.id >= startId }
                .take(count)
                .toList()
        }
        //в противном случае передаём количество сообщений с конца
        else {
            chat.getMessageList().asReversed().asSequence()
                .filter { !it.deleted }
                .take(count)
                .toList()
                .asReversed()
        }
    }

    //возвращаем последнее сообщение в указанном чате
    fun getLastMessage(chat: Chat): Message {
        return chat.getMessageList().last()
    }


    fun deleteChat(chat: Chat, initiator: ChatUser) {
        //если удаляющий присутствует в чате, то
        if (chat.isUserInChat(initiator)) {
            //удаляем чат из списка чатов у обоих пользователей
            val uList = chat.getUserList()
            uList[0].removeChatFromList(uList[1])
            uList[1].removeChatFromList(uList[0])
            //и помечаем чат удалённым
            chat.deleted = true
        } else {
            println("Указанный чат недоступен либо удалён")
        }
    }

    //возвращаем информацию о всех чатах пользователя с непрочитанными сообщениями
    fun getUnreadTotal(unreadList: List<Message>): HashMap<Chat, Int> {
        val result = HashMap<Chat, Int>()
        unreadList.forEach {
            if (!it.chat.deleted && !it.deleted) {
                result[it.chat] = result.getOrDefault(it.chat, 0) + 1
            }
        }
        return result
    }

}