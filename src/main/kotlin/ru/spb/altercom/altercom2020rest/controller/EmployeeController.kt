package ru.spb.altercom.altercom2020rest.controller

import org.springframework.web.bind.annotation.*
import ru.spb.altercom.altercom2020rest.model.Employee
import ru.spb.altercom.altercom2020rest.model.Message
import ru.spb.altercom.altercom2020rest.service.HttpClient

@CrossOrigin
@RestController
class EmployeeController {

    @GetMapping("/employees")
    fun getEmployees(): Collection<Employee> = HttpClient().getEmployeesList()

    @PostMapping("/message")
    fun sendMessage(@RequestBody message: Message) = HttpClient().sendNotification(message)
}
