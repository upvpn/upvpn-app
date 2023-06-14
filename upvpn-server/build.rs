fn main() -> Result<(), Box<dyn std::error::Error>> {
    tonic_build::configure()
        .build_server(false)
        .compile(&["proto/upvpn-server.proto"], &["proto"])?;
    println!("cargo:rerun-if-changed=proto");
    Ok(())
}
