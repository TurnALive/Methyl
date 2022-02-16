package live.turna.methyl.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.mizosoft.methanol.Methanol;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import live.turna.methyl.MethylLoader;
import live.turna.methyl.util.exceptions.HttpStatusException;
import live.turna.methyl.util.exceptions.RateLimitException;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;
import java.util.logging.Level;

public class YggdrasilUtil {
    private static final String MINESKIN_API = "https://api.mineskin.org";

    private static final HttpClient CLIENT = Methanol.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .userAgent("Methyl/1.1")
            .requestTimeout(Duration.ofSeconds(12))
            .readTimeout(Duration.ofSeconds(6))
            .build();

    @Nullable
    public static UUID usernameToUUID(String skinServer, String username) throws IOException, InterruptedException, HttpStatusException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(skinServer + "/api/yggdrasil/api/profiles/minecraft"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("[\"" + username + "\"]"))
                .build();

        HttpResponse<InputStream> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofInputStream());

        if (res.statusCode() != 200) {
            MethylLoader.getInstance().getLogger().log(Level.SEVERE, "Got HTTP status code " + res.statusCode() + " while querying UUID for " + username);
            throw new HttpStatusException(res.statusCode());
        }

        JsonArray arr = JsonParser.parseReader(new InputStreamReader(res.body())).getAsJsonArray();

        if (arr.isEmpty()) {
            MethylLoader.getInstance().getLogger().log(Level.WARNING, "Player " + username + " does not exist on skin server");
            return null;
        }

        JsonObject player = arr.get(0).getAsJsonObject();
        return Util.stringNoDashesToUUID(player.get("id").getAsString());
    }

    @Nullable
    public static String getTextureFromUsername(String skinServer, String username) throws IOException, InterruptedException, HttpStatusException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(skinServer + "/" + username + ".json"))
                .GET()
                .build();

        HttpResponse<InputStream> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofInputStream());

        int status = res.statusCode();
        if (status != 200) {
            if (status == 404)
                MethylLoader.getInstance().getLogger().log(Level.WARNING, "Player " + username + " does not exist on skin server");
            else
                MethylLoader.getInstance().getLogger().log(Level.SEVERE, "Got HTTP status code " + status + " while querying texture for " + username);

            throw new HttpStatusException(status);
        }

        JsonObject obj = JsonParser.parseReader(new InputStreamReader(res.body())).getAsJsonObject();
        JsonObject skins = obj.get("skins").getAsJsonObject();

        JsonElement texture = skins.get(skins.has("slim") ? "slim" : "default");
        if (texture.isJsonNull()) {
            MethylLoader.getInstance().getLogger().log(Level.WARNING, "Player " + username + " does not have a texture");
            return null;
        }

        return skinServer + "/textures/" + texture.getAsString();
    }

    public static ProfileProperty generateTextureFromMineSkin(String url) throws IOException, InterruptedException, HttpStatusException, RateLimitException {
        JsonObject reqBody = new JsonObject();
        reqBody.addProperty("visibility", 1);
        reqBody.addProperty("url", url);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(MINESKIN_API + "/generate/url"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(reqBody.toString()))
                .build();

        HttpResponse<InputStream> res = CLIENT.send(req, HttpResponse.BodyHandlers.ofInputStream());

        int status = res.statusCode();
        if (status != 200 && status != 429 && status != 400 && status != 500) {
            MethylLoader.getInstance().getLogger().log(Level.SEVERE, "Got HTTP status code " + status + " while generating texture from MineSkin, url: " + url);
            throw new HttpStatusException(status);
        }

        JsonObject obj = JsonParser.parseReader(new InputStreamReader(res.body())).getAsJsonObject();

        if (status == 429) {
            int delay = obj.get("delay").getAsInt();
            MethylLoader.getInstance().getLogger().log(Level.WARNING, "MineSkin API rate limit hit, need to wait " + delay + " seconds");
            throw new RateLimitException(delay);
        }

        if (status == 400 || status == 500) {
            String errorCode = obj.get("errorCode").getAsString();
            String errorMsg = obj.get("error").getAsString();
            MethylLoader.getInstance().getLogger().log(Level.SEVERE, "MineSkin API returned error: " + errorCode + " - " + errorMsg);
            throw new RuntimeException("MineSkin API error: " + errorCode + " - " + errorMsg);
        }

        JsonObject data = obj.get("data").getAsJsonObject().get("texture").getAsJsonObject();
        return new ProfileProperty(
                "textures",
                data.get("value").getAsString(),
                data.get("signature").getAsString()
        );
    }
}
