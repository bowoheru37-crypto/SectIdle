const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.standardTargetOptions(.{});
    const optimize = b.standardOptimizeOption(.{ .preferred_optimize_mode = .ReleaseSmall });

    const exe = b.addExecutable(.{
        .name = "NizamGame",
        .root_source_file = b.path("src/main.zig"),
        .target = target,
        .optimize = optimize,
    });
    exe.linkLibC();
    b.installArtifact(exe);

    // Auto build untuk Android & Web
    const run_cmd = b.addRunArtifact(exe);
    b.step("run", "Run Nizam Engine Game").dependOn(&run_cmd.step);

    // Platform step helpers
    const build_all_step = b.step("build-all", "Build all targets (PC, Android, WebGL, iOS)");
    build_all_step.dependOn(&exe.step);
}
