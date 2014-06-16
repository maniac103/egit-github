package org.eclipse.egit.github.core.client;

import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;

import java.lang.reflect.Type;
import java.util.Date;

import org.eclipse.egit.github.core.CommitComment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public class CommitCommentFormatter implements JsonDeserializer<CommitComment> {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateFormatter())
            .setFieldNamingPolicy(LOWER_CASE_WITH_UNDERSCORES).create();

    public CommitComment deserialize(JsonElement json, Type typeOfT,
            JsonDeserializationContext context) throws JsonParseException {
        final CommitComment commitComment = gson.fromJson(json, CommitComment.class);
        if (commitComment != null) {
            JsonElement position = json.getAsJsonObject().get("position");
            if (position == null || position.isJsonNull()) {
                commitComment.setPosition( -1 );
            }
        }
        return commitComment;
    }

}
