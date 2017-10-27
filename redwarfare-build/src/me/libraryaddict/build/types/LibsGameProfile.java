package me.libraryaddict.build.types;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

public class LibsGameProfile {
    private GameProfile _gameProfile;
    private UUID _joinedMap;
    private UUID _leftMap;

    public LibsGameProfile(UUID leftMap, UUID joinedMap, GameProfile gameProfile) {
        _leftMap = leftMap;
        _joinedMap = joinedMap;
        _gameProfile = gameProfile;
    }

    public UUID getMapJoined() {
        return _joinedMap;
    }

    public UUID getMapLeft() {
        return _leftMap;
    }

    public GameProfile getProfile() {
        return _gameProfile;
    }
}
