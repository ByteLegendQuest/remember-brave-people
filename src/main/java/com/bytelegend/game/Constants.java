package com.bytelegend.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class Constants {
    static final String DEFAULT_REPO_URL = "https://github.com/ByteLegendQuest/remember-brave-people.git";
    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    static final String HEROES_JSON = "heroes.json";
    static final String INPUT_HEROES_CURRENT_PNG = "build/input-heroes-current.png";
    static final String INPUT_HEROES_CURRENT_JSON = "build/input-heroes-current.json";
    static final String OUTPUT_HEROES_CURRENT_PNG = "build/heroes-current.png";
    static final String OUTPUT_HEROES_CURRENT_JSON = "build/heroes-current.json";
    static final String DEFAULT_S3_REGION = "ap-northeast-2";
    static final String DEFAULT_S3_BUCKET = "brave-people";
    static final String PUBLIC_HEROES_CURRENT_IMAGE_URL = "https://brave-people.s3.ap-northeast-2.amazonaws.com/heroes-current.png";
    static final String PUBLIC_HEROES_CURRENT_JSON_URL = "https://brave-people.s3.ap-northeast-2.amazonaws.com/heroes-current.json";

    static final int IMAGE_GRID_WIDTH = 20;
    static final int IMAGE_GRID_HEIGHT = 20;
    static final int TILE_WIDTH_PIXEL = 62;
    static final int TILE_HEIGHT_PIXEL = 62;
    static final int TILE_BORDER_PIXEL = 2;
    static final int TILE_WITH_BORDER_WIDTH = TILE_WIDTH_PIXEL + 2 * TILE_BORDER_PIXEL;
    static final int TILE_WITH_BORDER_HEIGHT = TILE_HEIGHT_PIXEL + 2 * TILE_BORDER_PIXEL;

    static final String CI_BASE_REF = "origin/main";
}
