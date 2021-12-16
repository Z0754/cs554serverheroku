package com.example.cs554serverheroku

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.util.*

@SpringBootApplication
class serverApplication

var defaultPageID: String = "1"
@Serializable
data class Component(val id: String, val variant: String, val expiry: String, val itemDataModel: ItemDataModel)
@Serializable
data class Layout(val pageID: String, val expiry: String, val components: List<Component>)
@Serializable
data class ItemDataModel(val id: String, var text: String, var color:String, val itemName: String, val imageURL:String,
                         val iconKey: String, val action: String, val payload: List<String>)
@Serializable
data class DataModelUpdateRequest(val pageID: String, val id: String)

var conn: Connection? = null

var layout_dict: MutableMap<String, Layout> = mutableMapOf()
val layouts_json: MutableList<String> = mutableListOf()


var page_ori = Layout("1", "6000", listOf(
    Component("3265", "label", "6000",
        ItemDataModel("3254","this is a label","red","","","","",
            listOf("payload1","payload2"))
    ),
    Component("3265", "label", "6000",
        ItemDataModel("3244","this is a another label","green","","","","",
            listOf("payload1","payload2"))
    ),
    Component("4765", "button", "6000",
        ItemDataModel("5249","Tap me","blue","","","","action1",
            listOf())
    ),
    Component("4327", "button", "6000",
        ItemDataModel("3876","tap me","green","","","","action2",
            listOf())
    ),
    Component("1239", "list", "6000",
        ItemDataModel("7654","","red","","","","",
            listOf("item1","item2","item3"))
    ),
    Component("4128", "picker", "6000",
        ItemDataModel("7645","Select Item","red","Style3","","","",
            listOf("a","b","c"))
    ),
    Component("6645", "icon", "6000",
        ItemDataModel("9344","sun rise","","","","sunrise","",
            listOf())
    )))


var page01 = Layout("1", "6000", listOf(
    Component("3265", "label", "6000",
        ItemDataModel("3244","this is a label","red","","","","",
            listOf("payload1","payload2"))
    ),
    Component("5165", "label", "6000",
        ItemDataModel("4243","this is another label","green","","","","",
            listOf())
    ),
    Component("4765", "button", "6000",
        ItemDataModel("5249","Count!","red","","","icloud.and.arrow.up.fill","action1",
            listOf())
    ),
    Component("4327", "button", "6000",
        ItemDataModel("3876","tap me","blue","","","","action2",
            listOf())
    ),

    Component("4128", "picker", "6000",
        ItemDataModel("7645","Select Item","blue","Style2","","","",
            listOf("a","b","c"))
    ),
    Component("6645", "icon", "6000",
        ItemDataModel("9344","sun rise","","","","sunrise","",
            listOf())
    ),
    Component("1239", "list", "6000",
        ItemDataModel("7654","","red","","","","",
            listOf("apple","banana","peach"))
    )
))

var page01_new = Layout("1", "6000", listOf(
    Component("4765", "button", "6000",
        ItemDataModel("5249","","red","","","icloud.and.arrow.up.fill","action1",
            listOf())
    ),
    Component("428", "picker", "6000",
        ItemDataModel("745","Select Item","blue","Style3","","","",
            listOf("item1","item2","item3"))
    ),
    Component("3265", "label", "6000",
        ItemDataModel("3244","hello","red","","","","",
            listOf("payload1","payload2"))
    ),
    Component("5165", "label", "6000",
        ItemDataModel("4243","this is another view!","green","","","","",
            listOf())
    ),
    Component("6645", "icon", "6000",
        ItemDataModel("9344","cloud.sun.rain.fill","","","","cloud.sun.rain.fill","",
            listOf())
    ),
    Component("1239", "list", "6000",
    ItemDataModel("7654","","red","","","","",
        listOf("hello","world","developer","view"))
)
))


fun main(args: Array<String>) {
    layout_dict["1"] = page01
//    getConnection()
//    conn?.let { initTable(it) }
    runApplication<serverApplication>(*args)
}



@RestController
class HelloController {

    @GetMapping("/", produces = [MediaType.TEXT_HTML_VALUE])
    fun theAnswer(): String = "<h1>Hello, World</h1>"
}

@RestController
class LayoutTest01Resource {

    @GetMapping(value = ["/LayoutTest01"])
    fun sampleLayout1(): Layout? {

        return page_ori
    }
}

@RestController
class LayoutTest03Resource {
    @PostMapping(value = ["/LayoutTest03"])
    fun sampleLayout3(): Layout {
        return page01_new
    }
}


@RestController
class EchoPayloadResource {
    @PostMapping(value = ["/EchoPayload"])
    fun echo(@RequestBody payload: String): String{
        return payload
    }
}

@RestController
class LayoutUpload {
    @PostMapping(value = ["/LayoutUpload"])
    fun uploadLayout(@RequestBody payload: String): String{
        val layout_toAdd = Json.decodeFromString<Layout>(payload)
        if(layout_dict.containsKey(layout_toAdd.pageID)){
            return "invalid pageid, follow id are occupied"+ layout_dict.keys
        }
        layout_dict[layout_toAdd.pageID] = layout_toAdd

    return "pageID: "+layout_toAdd.pageID
    }
}

@RestController
class LayoutUpdate {
    @GetMapping(value = ["/LayoutUpdate/{layoutnum}"])
    fun getLayout(@PathVariable(value="layoutnum") k: String): Layout? {
        if (layout_dict.containsKey(k))
            return layout_dict[k]
        else
            return page01_new
    }
}

@RestController
class LayoutResourceGetter {
    @GetMapping(value = ["/LayoutResource/{requested_resource}"])
    fun getLayout(@PathVariable(value="requested_resource") k: String): Layout? {
        if (layout_dict.containsKey(k)) {
            defaultPageID = k
            page01 = layout_dict[k]!!
            return page01
        }else {
            return Layout("0000", "6000", listOf())
        }
    }
}

@RestController
class ItemModelUpdate {
    @PostMapping(value = ["/ItemModelUpdate"])
    fun dataModelUpdate(@RequestBody payload: String): Layout{
        val update_request = Json.decodeFromString<DataModelUpdateRequest>(payload)
        if (layout_dict.containsKey(update_request.pageID)){
            val toUpdate = layout_dict[update_request.pageID]
            for (c in toUpdate!!.components){
                if (c.itemDataModel.id == update_request.id){
                    when(c.itemDataModel.color){
                        "red" -> c.itemDataModel.color = "green"
                        "green" -> c.itemDataModel.color = "blue"
                        "blue" -> c.itemDataModel.color = "red"
                    }

                    break
                }
            }
            layout_dict[update_request.pageID] = toUpdate

            return toUpdate
        }else{
            return page01_new
        }


    }
}