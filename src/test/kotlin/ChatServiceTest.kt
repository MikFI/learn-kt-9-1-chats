import chat.ChatService
import org.junit.Before
import org.junit.Test

class ChatServiceTest {
    val chat = ChatService()
    val u1 = chat.createUser()
    val u2 = chat.createUser()
    val u3 = chat.createUser()

    @Before
    fun addMessages(){
        u1.newMessage(u2, "qqq1")
        u1.newMessage(u3, "www")
    }

    @Test
    fun testChats(){
        //проверяем создание чата вместе с написанием сообщения
        assert(u1.getChatsAll().size == 2)

        //проверяем удаление своего чата
        val c1 = u1.getChat(u2)!!
        u2.deleteChat(c1)
        assert(u1.getChatsAll().size == 1)
    }

    @Test
    fun testMessages() {
        val c1 = u1.getChat(u2)!!

        //проверяем удаление своего сообщения
        u1.newMessage(u2, "qqq2")
        var m1 = u1.getMessages(c1)!![0]
        u1.deleteMessage(c1, m1)
        assert(c1.getMessageCount() == 1) // счётчик сообщений равен 1

        //проверяем удаление чужого сообщения
        m1 = u1.getMessages(c1)!![0]
        u2.deleteMessage(c1, m1)
        assert(c1.getMessageCount() == 1) //счётчик сообщений всё ещё равен 1

        //проверяем редактирование своего сообщения
        u1.editMessage(m1, "ZZZ")
        assert(m1.text == "ZZZ") // сообщение отредактировано

        //проверяем редактирование чужого сообщения
        u2.editMessage(m1, "ZZZ")
        assert(m1.text == "ZZZ") // сообщение НЕ отредактировано

        //получаем все непрочитанные во всех чатах
        u1.newMessage(u2, "qqq3")
        u1.newMessage(u2, "qqq4")
        u3.newMessage(u2, "bbb")
        val result1 = u2.getUnreadList()
        assert(result1.size == 3)

        //проверяем вывод нужного числа сообщений из чата, начиная с указанного id
        var result3 = u1.getMessages(c1, 0, 2)
        assert(result3!![1].text == "qqq3" && result3.size == 2)

        //проверяем вывод нужного числа последних сообщений из чата
        result3 = u1.getMessages(c1, -1, 2)
        assert(result3!![1].text == "qqq4" && result3.size == 2)

        //вытаскиваем все последние сообщения со всех чатов у пользователя
        result3 = u2.getLastMessages()
        assert(result3.size == 2)

    }
}