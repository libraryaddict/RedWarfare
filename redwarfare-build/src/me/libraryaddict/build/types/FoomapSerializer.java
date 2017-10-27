package me.libraryaddict.build.types;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

public final class FoomapSerializer implements JsonSerializer<Multimap>, JsonDeserializer<Multimap> {
    private static final Type type = new TypeToken<HashMap<String, Collection<Property>>>() {
    }.getType();

    public static LibsGameProfile fromGson(String string) {
        return getBuilder().fromJson(string, LibsGameProfile.class);
    }

    private static Gson getBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        gsonBuilder.registerTypeAdapter(Multimap.class, new FoomapSerializer());
        gsonBuilder.registerTypeAdapter(HashMap.class, new InstanceCreator<HashMap>() {
            @Override
            public HashMap createInstance(Type type) {
                return new HashMap();
            }
        });

        return gsonBuilder.create();
    }

    public static String toGson(LibsGameProfile profile) {
        return getBuilder().toJson(profile);
    }

    private boolean doneParent;

    @Override
    public Multimap deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        HashMap<String, Collection<Property>> map = new Gson().fromJson(json, type);
        Multimap multimap;

        if (!doneParent) {
            doneParent = true;

            multimap = LinkedHashMultimap.create();

            for (Entry<String, Collection<Property>> entry : map.entrySet()) {
                multimap.putAll(entry.getKey(), entry.getValue());
            }
        } else {
            multimap = new PropertyMap();

            for (Entry<String, Collection<Property>> entry : map.entrySet()) {
                multimap.putAll(entry.getKey(), entry.getValue());
            }
        }

        return multimap;
    }

    @Override
    public JsonElement serialize(Multimap multimap, Type typeOfT, JsonSerializationContext jsonContext) {
        return jsonContext.serialize(new HashMap(multimap.asMap()), type);
    }
}