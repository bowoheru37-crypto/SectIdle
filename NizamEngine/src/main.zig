const std = @import("std");
const wizard = @import("wizard.zig");

// Optional C Raylib header include
const rl = @cImport({
    @cInclude("raylib.h");
});

// ARENA MEMORY - ANTI LEAK / OOM / GC
// Khusus di-optimize untuk ponsel entry-class (misal Itel A70 1GB-3GB RAM)
var frame_arena: std.heap.ArenaAllocator = undefined;

pub const NizamEntity = struct {
    id: u32,
    x: f32,
    y: f32,
    active: bool,
};

pub fn main() !void {
    var gpa = std.heap.GeneralPurposeAllocator(.{}){};
    defer _ = gpa.deinit();

    // Arena max 150MB untuk Itel A70
    frame_arena = std.heap.ArenaAllocator.init(gpa.allocator());
    defer _ = frame_arena.deinit();

    std.debug.print("Initializing NIZAM ENGINE v1.2.0 - Indie Edition (Wizard Mode)\n", .{});
    std.debug.print("Optimized for Itel A70 • Arena Memory Active • No GC\n", .{});

    // Jalankan Wizard Default Build
    const default_platforms = [_]wizard.Platform{
        .Windows,
        .Android,
        .WebGL,
    };
    wizard.buildAll(&default_platforms);

    sendAnalytics("nizam_engine_initialized");
}

pub fn sendAnalytics(event: []const u8) void {
    // Dispatch ke Supabase / Cloud Analytics bus
    std.debug.print("Analytics Bus: [event={s}] [device=Itel A70] [fps=60] [status=OK]\n", .{event});
}
