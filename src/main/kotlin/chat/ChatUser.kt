package chat

class ChatUser(
    //Идентификатор пользователя
    private val id: Int = 0,
    //Список чатов, в которых состоит пользователь
    //внутри этого списка хранится список непрочитанных сообщений
//    val chatList: MutableList<Pair<Chat.ChatUser,Chat.Chat>> = mutableListOf(),
    private val chatList: HashMap<ChatUser, Chat> = hashMapOf(),
    private val unreadList: MutableList<Message> = mutableListOf(),
    //указываем сервис, которому принадлежит наш пользователь
    //для того, чтобы создание чата работало через него, а не через сам чатсервис
    //(сообщение кому-то посылает пользователь, а не чат)
    private val chatService: ChatService
) {
    override fun toString(): String {
        return "$id"
    }

    fun getId(): Int {
        return id
    }

    fun getChatService(): ChatService {
        return chatService
    }

    fun getUnreadList(): List<Message> {
        return unreadList
    }


    //пишем сообщение пользователю
    fun newMessage(user: ChatUser, msgText: String) {
        //ищем адресата в списке чатов отправителя
        val toUser = chatList.keys.find { it == user }
        val chat: Chat
        //если не найден, создаём чат, суём его в список чатов себе и тому, кому написали
        if (toUser == null) {
            chat = chatService.createChat(listOf(this, user)) ?: return
            chatList[user] = chat
            chatService.addUserToChat(fromUser = this, toUser = user, chat)
        } else {
            chat = chatList[toUser]!! //тут можно без not-null assertion обойтись?

        }
        //отправляем сообщение в чат пользователю
        chatService.newMessage(chat, this, msgText)
        //автоматически читаем в этом чате всё непрочитанное
        //если мы что-то в него отправили, значит открыли, а значит и прочитали
        readChat(chat)
    }

    fun addChat(fromUser: ChatUser, chat: Chat) {
        chatList[fromUser] = chat
    }

    fun addUnreadMessage(message: Message) {
        this.unreadList.add(message)
    }

    //редактируем сообщение
    fun editMessage(message: Message, newText: String) {
        chatService.editMessage(this, message, newText)
    }

    //удаляем сообщение из указанного чата
    fun deleteMessage(chat: Chat, message: Message) {
        chatService.deleteMessage(message, this)
        //при удалении сообщения логика та же
        //если мы что-то удаляем в чате - значит мы в нём находимся,
        //а значит читаем всё непрочитанное
        readChat(chat)
    }

    //читаем из чата первые count неудалённых сообщений, начиная со startId
    fun getMessages(chat: Chat, startId: Int = 0, count: Int = 5): List<Message>? {
        return chatService.getMessageList(chat, this, startId, count)
    }

    //читаем последнее сообщение из каждого чата
    fun getLastMessages(): List<Message> {
        val result = mutableListOf<Message>()
        //все существующие в списке пользователя чаты должны иметь минимум одно сообщение - поэтому тут !!
        chatList.forEach { result.add(chatService.getLastMessage(it.value)) }
        return result
    }

    //удаляем указанный чат
    fun deleteChat(chat: Chat) {
        chatService.deleteChat(chat, this)
    }

    fun removeChatFromList(user: ChatUser) {
        chatList.remove(key = user)
    }

    //получаем список чатов (кроме удалённых)
    fun getChatsAll(): MutableList<Chat> {
        val result = mutableListOf<Chat>()
        chatList.forEach {
            if (!it.value.deleted) {
                result.add(it.value)
            }
        }
        return result
    }

    //проверяем наличие чата с запрошенным пользователем
    fun getChat(user: ChatUser): Chat? {
        val result = chatList[user]
        if (result == null) println("Чата с этим пользователем нет")
        return result
    }

    //читаем все непрочитанные сообщения в указанном чате
    private fun readChat(chat: Chat): List<Message>? {
        if (chat !in chatList.values) return null
        chatService.getMessageList(chat, this)
        if (chat.deleted) {
//            println("Чат удалён")
            return null
        }
        val result: List<Message> = unreadList.filter { it.chat == chat && !it.deleted }
        unreadList.removeIf { it.chat == chat }
        return result
    }
}