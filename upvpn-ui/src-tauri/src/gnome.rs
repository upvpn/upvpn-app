pub fn is_gnome() -> bool {
    #[cfg(target_os = "linux")]
    {
        std::env::var("XDG_CURRENT_DESKTOP")
            .unwrap_or_default()
            .to_lowercase()
            .contains("gnome")
    }
    #[cfg(not(target_os = "linux"))]
    {
        false
    }
}
