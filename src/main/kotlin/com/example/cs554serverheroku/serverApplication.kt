package com.example.cs554serverheroku

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@SpringBootApplication
class serverApplication

@Serializable
data class Component(val id: String, val variant: String, val expiry: String, val itemDataModel: ItemDataModel)
@Serializable
data class Layout(val pageID: String, val expiry: String, val components: List<Component>)
@Serializable
data class ItemDataModel(val id: String, var text: String, val color:String, val itemName: String, val imageURL:String,
                         val iconKey: String, val action: String, val payload: List<String>)
@Serializable
data class DataModelUpdateRequest(val pageID: String, val id: String)


val layout_dict: MutableMap<String, Layout> = mutableMapOf()
val layouts_json: MutableList<String> = mutableListOf()
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
        ItemDataModel("5249","tap me","red","","","","action1",
            listOf())
    ),
    Component("4327", "button", "6000",
        ItemDataModel("3876","tap me","blue","","","","action2",
            listOf())
    ),
    Component("1239", "list", "6000",
        ItemDataModel("7654","","red","","","","",
            listOf("item1","item2"))
    ),
    Component("6645", "icon", "6000",
        ItemDataModel("9344","sun rise","","","","sunrise","",
            listOf())
    )
))

var page01_new = Layout("1", "6000", listOf(
    Component("3265", "label", "6000",
        ItemDataModel("2","label updated","red","","","","",
            listOf("payload1","payload2"))
    ),
    Component("5165", "label", "6000",
        ItemDataModel("4243","this is another label","green","","","","",
            listOf())
    ),
    Component("4765", "button", "6000",
        ItemDataModel("5249","tap me","red","","","","action1",
            listOf())
    ),
    Component("4327", "button", "6000",
        ItemDataModel("3876","tap me","blue","","","","action2",
            listOf())
    ),
    Component("1239", "list", "6000",
        ItemDataModel("7654","","red","","","","",
            listOf("item1","item2"))
    ),
    Component("6645", "icon", "6000",
        ItemDataModel("9344","sun rise","","","","sunrise","",
            listOf())
    )
))



fun main(args: Array<String>) {
    layout_dict["1"] = page01
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
        return layout_dict["1"]
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
class LayoutResourceGetter {
    @GetMapping(value = ["/LayoutResource/{requested_resource}"])
    fun getLayout(@PathVariable(value="requested_resource") k: String): Layout? {
        if (layout_dict.containsKey(k))
            return layout_dict[k]
        else
            return Layout("0000","6000", listOf())
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
                    c.itemDataModel.text += " updated"
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