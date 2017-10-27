package me.libraryaddict.core.fancymessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;

import com.google.gson.stream.JsonWriter;

/**
 * Represents a JSON string value. Writes by this object will not write name values nor begin/end objects in the JSON stream. All
 * writes merely write the represented string value.
 */
final class JsonString implements JsonRepresentedObject, ConfigurationSerializable
{

    public static JsonString deserialize(Map<String, Object> map)
    {
        return new JsonString(map.get("stringValue").toString());
    }

    private String _value;

    public JsonString(CharSequence value)
    {
        _value = value == null ? null : value.toString();
    }

    public String getValue()
    {
        return _value;
    }

    public Map<String, Object> serialize()
    {
        HashMap<String, Object> theSingleValue = new HashMap<String, Object>();
        theSingleValue.put("stringValue", _value);
        return theSingleValue;
    }

    @Override
    public String toString()
    {
        return _value;
    }

    @Override
    public void writeJson(JsonWriter writer) throws IOException
    {
        writer.value(getValue());
    }
}
