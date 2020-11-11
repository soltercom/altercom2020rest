package ru.spb.altercom.altercom2020rest.service

import org.springframework.http.*
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.w3c.dom.Document
import org.w3c.dom.Node
import ru.spb.altercom.altercom2020rest.model.Employee
import ru.spb.altercom.altercom2020rest.model.Message
import sun.misc.BASE64Encoder
import java.nio.charset.StandardCharsets
import javax.xml.parsers.DocumentBuilderFactory

class HttpClient {

    private val rest: RestTemplate = RestTemplate()
    private val headers: HttpHeaders = HttpHeaders()

    private val url = "https://cus.buhphone.com/cus/ws/BotAPI"
    private val user = "Altercom20201005"
    private val password = "e508d6bc"

    private fun getEmployeeListRequest() = """
        <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope" xmlns:bot="http://buhphone.com/BotAPI" xmlns:core="http://v8.1c.ru/8.1/data/core" xmlns:xsi="xsi">
            <soap:Header/>
            <soap:Body>
                <bot:GetEmployeesList>
                    <bot:Params>
                        <Property name="InfobaseID" xmlns="http://v8.1c.ru/8.1/data/core">
                            <Value xsi:type="xs:string">88232313-2fbf-0000-bab7-7054d21ab6ff</Value>
                        </Property>
                    </bot:Params>
                </bot:GetEmployeesList>
            </soap:Body>
        </soap:Envelope>
        """
    private fun sendNotificationRequest(employeeId: String, author: String, phone: String, text: String) = """
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
            <soap:Body>
                <m:SendNotification xmlns:m="http://buhphone.com/BotAPI">
                    <m:Params xmlns:xs="http://www.w3.org/2001/XMLSchema"
                            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
                        <Property xmlns="http://v8.1c.ru/8.1/data/core"
                                name="InfobaseID">
                            <Value xsi:type="xs:string">72d4b1cf-0000-11e9-0001-ba5e5a7280d8</Value>
                        </Property>
                        <Property xmlns="http://v8.1c.ru/8.1/data/core"
                                name="Recipients">
                            <Value xsi:type="ValueTable">
                                <column>
                                    <Name>EmployeeID</Name>
                                    <ValueType/>
                                </column>
                                <column>
                                    <Name>SupportLineID</Name>
                                    <ValueType/>
                                </column>
                                <column>
                                    <Name>DescriptionURL</Name>
                                    <ValueType/>
                                </column>
                                <column>
                                    <Name>DescriptionHint</Name>
                                    <ValueType/>
                                </column>
                                <row>
                                    <Value xsi:type="xs:string">${employeeId}</Value>
                                    <Value xsi:type="xs:string">da75063a-b23b-11e7-80e6-0025904f970f</Value>
                                    <Value xsi:type="xs:string"/>
                                    <Value xsi:type="xs:string"/>
                                </row>
                            </Value>
                        </Property>
                        <Property xmlns="http://v8.1c.ru/8.1/data/core"
                                name="Text">
                            <Value xsi:type="xs:string">${text}</Value>
                        </Property>
                        <Property xmlns="http://v8.1c.ru/8.1/data/core"
                                name="Subject">
                            <Value xsi:type="xs:string">${author} (${phone})</Value>
                        </Property>
                        <Property xmlns="http://v8.1c.ru/8.1/data/core"
                                name="Name">
                            <Value xsi:type="xs:string">${author}</Value>
                        </Property>
                    </m:Params>
                </m:SendNotification>
            </soap:Body>
        </soap:Envelope>
    """.trimIndent()


    fun getEmployeesList(): List<Employee> {
        val employeeList = mutableListOf<Employee>()

        val requestEntity = HttpEntity(getEmployeeListRequest(), headers)
        val responseEntity: ResponseEntity<String> = rest.exchange<String>(url, HttpMethod.POST, requestEntity, String::class.java)
        val xmlDoc: Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(responseEntity.body?.byteInputStream())
        xmlDoc.documentElement.normalize()

        val rowList = xmlDoc.getElementsByTagName("row")
        for (i in 0 until rowList.length) {
            val rowNode = rowList.item(i)
            val valueList = mutableListOf<String>()
            for (j in 0 until rowNode.childNodes.length) {
                val valueNode = rowNode.childNodes.item(j)
                if (valueNode.nodeType == Node.ELEMENT_NODE && valueNode.nodeName == "Value")
                    valueList.add(valueNode.textContent)
            }
            val employee = Employee(valueList)
            employeeList.add(employee)
        }

        return employeeList
    }

    fun sendNotification(message: Message): Int {
        val requestEntity = HttpEntity(sendNotificationRequest(message.employeeId, message.author, message.phone, message.text), headers)
        val responseEntity: ResponseEntity<String> = rest.exchange<String>(url, HttpMethod.POST, requestEntity, String::class.java)
        return responseEntity.statusCodeValue
    }

    init {
        val enc = BASE64Encoder()
        val userpassword = "$user:$password"
        val encodedAuthorization = enc.encode(userpassword.toByteArray())
        headers.setBasicAuth(encodedAuthorization)
        rest.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
    }
}
