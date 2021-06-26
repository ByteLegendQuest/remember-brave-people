package com.bytelegend.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class Constants {
    static final String DEFAULT_REPO_URL = "https://github.com/ByteLegendQuest/remember-brave-people.git";
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    static final String BRAVE_PEOPLE_JSON = "brave-people.json";
    static final String BRAVE_PEOPLE_PNG = "brave-people.png";
    static final String INPUT_BRAVE_PEOPLE_PNG = "build/input-brave-people.png";
    static final String BRAVE_PEOPLE_ALL_JSON = "brave-people-all.json";
    static final String INPUT_BRAVE_PEOPLE_ALL_JSON = "build/input-brave-people-all.json";
    static final String OUTPUT_BRAVE_PEOPLE_PNG = "build/brave-people.png";
    static final String OUTPUT_BRAVE_PEOPLE_ALL_JSON = "build/brave-people-all.json";
    static final String DEFAULT_OSS_ENDPOINT = "https://oss-cn-hongkong.aliyuncs.com";
    static final String DEFAULT_OSS_BUCKET = "bytelegend-brave-people";
    static final String PUBLIC_BRAVE_PEOPLE_IMAGE_URL = "https://bytelegend-brave-people.oss-cn-hongkong.aliyuncs.com/brave-people.png";
    static final String PUBLIC_BRAVE_PEOPLE_JSON_ALL_URL = "https://bytelegend-brave-people.oss-cn-hongkong.aliyuncs.com/brave-people-all.json";

    static final int IMAGE_GRID_WIDTH = 20;
    static final int IMAGE_GRID_HEIGHT = 20;
    static final int TILE_WIDTH_PIXEL = 62;
    static final int TILE_HEIGHT_PIXEL = 62;
    static final int TILE_BORDER_PIXEL = 2;
    static final int TILE_WITH_BORDER_WIDTH = TILE_WIDTH_PIXEL + 2 * TILE_BORDER_PIXEL;
    static final int TILE_WITH_BORDER_HEIGHT = TILE_HEIGHT_PIXEL + 2 * TILE_BORDER_PIXEL;

    static final String CI_BASE_REF = "origin/master";
}
