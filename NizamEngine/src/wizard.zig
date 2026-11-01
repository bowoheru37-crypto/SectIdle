pub const WizardStep = enum(u8) {
    ProjectSetup = 0,
    CharacterPreview = 1,
    PlatformSelection = 2,
    BuildExport = 3,
};

pub const Platform = enum {
    Windows,
    MacOS,
    Linux,
    WebGL,
    Android,
    IOS,
};

pub const WizardConfig = struct {
    game_genre: []const u8 = "RPG Cultivation Nusantara",
    character_name: []const u8 = "Male Body Cultivator - Jade Dragon",
    world_biome: []const u8 = "Candi Prambanan",
    has_joystick: bool = true,
    has_jump: bool = true,
    has_keris_system: bool = true,
    has_batik_formation: bool = true,
    has_cloud_save: bool = true,
    has_analytics: bool = true,
    polygon_count: u32 = 12432, // LOD0 12.4K tris
    memory_limit_mb: u32 = 150, // Itel A70 hard limit
};

pub fn buildAll(platforms: []const Platform) void {
    for (platforms) |p| {
        switch (p) {
            .Android => buildAPK(),
            .Windows => buildEXE(),
            .WebGL => buildHTML(),
            .MacOS => buildDMG(),
            .Linux => buildAppImage(),
            .IOS => buildIPA(),
        }
    }
}

pub fn buildAPK() void {
    // Android build pipeline (3.5MB optimized for Itel A70)
}

pub fn buildEXE() void {
    // Windows 64-bit standalone executable
}

pub fn buildHTML() void {
    // WebGL / WASM HTML5 canvas bundle
}

pub fn buildDMG() void {
    // macOS Universal binary
}

pub fn buildAppImage() void {
    // Linux AppImage standalone
}

pub fn buildIPA() void {
    // iOS Application Package
}
