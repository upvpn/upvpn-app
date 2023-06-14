// Copyright (C) 2022 Mullvad VPN AB, GPL-3.0
use std::{ffi::CStr, io};

/// name of the group that should be excluded
const EXCLUSION_GROUP: &[u8] = b"upvpn-exclusion\0";

/// Bump filehandle limit
pub fn bump_filehandle_limit() {
    let mut limits = libc::rlimit {
        rlim_cur: 0,
        rlim_max: 0,
    };
    // SAFETY: `&mut limits` is a valid pointer parameter for the getrlimit syscall
    let status = unsafe { libc::getrlimit(libc::RLIMIT_NOFILE, &mut limits) };
    if status != 0 {
        tracing::error!(
            "Failed to get file handle limits: {}-{}",
            io::Error::from_raw_os_error(status),
            status
        );
        return;
    }

    const INCREASED_FILEHANDLE_LIMIT: u64 = 1024;
    // if file handle limit is already big enough, there's no reason to decrease it.
    if limits.rlim_cur >= INCREASED_FILEHANDLE_LIMIT {
        return;
    }

    limits.rlim_cur = INCREASED_FILEHANDLE_LIMIT;
    // SAFETY: `&limits` is a valid pointer parameter for the getrlimit syscall
    let status = unsafe { libc::setrlimit(libc::RLIMIT_NOFILE, &limits) };
    if status != 0 {
        tracing::error!(
            "Failed to set file handle limit to {}: {}-{}",
            INCREASED_FILEHANDLE_LIMIT,
            io::Error::from_raw_os_error(status),
            status
        );
    }
}

/// Returns the GID of `upvpn-exclusion` group if it exists.
pub fn get_exclusion_gid() -> io::Result<u32> {
    let exclusion_group_name = CStr::from_bytes_with_nul(EXCLUSION_GROUP).unwrap();
    get_group_id(exclusion_group_name)
}

/// Attempts to set the GID of the current process to `upvpn-exclusion`.
pub fn set_exclusion_gid() -> io::Result<u32> {
    let gid = get_exclusion_gid()?;
    set_gid(gid)?;
    Ok(gid)
}

/// Returns the GID of the specified group name
fn get_group_id(group_name: &CStr) -> io::Result<u32> {
    // SAFETY: group_name is a valid CString
    let group = unsafe { libc::getgrnam(group_name.as_ptr() as *const _) };
    if group.is_null() {
        return Err(io::Error::from(io::ErrorKind::NotFound));
    }
    // SAFETY: group is not null
    let gid = unsafe { (*group).gr_gid };
    Ok(gid)
}

/// Sets group ID for the current process
fn set_gid(gid: u32) -> io::Result<()> {
    if unsafe { libc::setgid(gid) } == 0 {
        Ok(())
    } else {
        Err(io::Error::last_os_error())
    }
}
