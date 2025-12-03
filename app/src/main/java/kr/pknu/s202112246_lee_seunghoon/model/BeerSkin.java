package kr.pknu.s202112246_lee_seunghoon.model;

public enum BeerSkin {
    DEFAULT(0, "기본 라거", "#FFC107", "#FFF4E6", 0),
    AMBER(1, "앰버 에일", "#FF8A00", "#FFE0B2", 10),
    DARK(2, "다크 비어", "#3E2723", "#D7CCC8", 10),
    BLUE(3, "블루 라군", "#03A9F4", "#E1F5FE", 20),
    MELON(4, "메론 소다", "#00E676", "#FFFFFF", 20),
    COLA(5, "콜라", "#2C0E0E", "#D7CCC8", 15);

    public final int id;   // 저장될 아이디
    public final String name;
    public final String liquidColor;
    public final String foamColor;
    public final int price;   // 가격

    BeerSkin(int id, String name, String liquidColor, String foamColor, int price) {
        this.id = id;
        this.name = name;
        this.liquidColor = liquidColor;
        this.foamColor = foamColor;
        this.price = price;
    }

    public static BeerSkin fromId(int id) {
        for (BeerSkin skin : values()) {
            if (skin.id == id) return skin;
        }
        return DEFAULT;
    }
}

