package app.upvpn.upvpn.data

import android.os.Build
import android.util.Log
import androidx.room.withTransaction
import app.upvpn.upvpn.data.db.Device
import app.upvpn.upvpn.data.db.User
import app.upvpn.upvpn.data.db.VPNDatabase
import app.upvpn.upvpn.data.db.toDbLocation
import app.upvpn.upvpn.data.db.toDeviceInfo
import app.upvpn.upvpn.data.db.toModelLocation
import app.upvpn.upvpn.model.AddDeviceRequest
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.UserCredentials
import app.upvpn.upvpn.network.VPNApiService
import app.upvpn.upvpn.network.toResult
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.fold
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

interface VPNRepository {
    suspend fun initDevice()
    suspend fun isAuthenticated(): String?
    suspend fun addDevice(userCredentials: UserCredentials): Result<String, String>
    suspend fun signOut(): Result<Unit, String>
    suspend fun getLocations(): Result<List<Location>, String>
    suspend fun getUser(): User?
    fun getRecentLocations(limit: Int): Flow<List<Location>>
}

class DefaultVPNRepository(
    private val vpnApiService: VPNApiService,
    private val vpnDatabase: VPNDatabase
) : VPNRepository {

    private val tag = "DefaultVPNRepository"

    private fun createDevice(): Device {
        val keyPair = KeyPair()
        val device = Device(
            uniqueId = UUID.randomUUID(),
            name = "${Build.MANUFACTURER} ${Build.PRODUCT}",
            version = Build.VERSION.RELEASE,
            arch = System.getProperty("os.arch") ?: "UNKNOWN",
            privateKey = keyPair.privateKey.toBase64()
        )
        Log.i(
            tag,
            "New device name: ${device.name} uniqueId: ${device.uniqueId} version: ${device.version} arch: ${device.arch}"
        )
        return device
    }

    override suspend fun initDevice() {
        val device = vpnDatabase.deviceDao().getDevice()
        if (device == null) {
            Log.i(tag, "Initializing device")
            vpnDatabase.deviceDao().insert(createDevice())
        } else {
            Log.i(tag, "Device already initialized ipv4 ${device.ipv4Address}")
        }
    }

    override suspend fun isAuthenticated(): String? {
        val user = vpnDatabase.userDao().getUser()
        return user?.email
    }

    override suspend fun addDevice(userCredentials: UserCredentials): Result<String, String> {
        // initialize device when user signs out and sign in again
        initDevice()
        val device = vpnDatabase.deviceDao().getDevice()!!
        val addDeviceResponse =
            vpnApiService.addDevice(AddDeviceRequest(userCredentials, device.toDeviceInfo()))
                .toResult().mapError { e -> e.message }

        addDeviceResponse.onSuccess {
            val updatedDevice =
                device.copy(ipv4Address = it.deviceAddresses.ipv4Address.hostAddress)
            vpnDatabase.withTransaction {
                val user = User(email = userCredentials.email, token = it.token)
                vpnDatabase.userDao().insert(user)
                vpnDatabase.deviceDao().update(updatedDevice)
            }
        }

        return addDeviceResponse.map { it.token }
    }

    override suspend fun signOut(): Result<Unit, String> {
        val signedOut = vpnApiService.signOut().toResult().mapError { e -> e.message }

        signedOut.onSuccess {
            vpnDatabase.withTransaction {
                val device = vpnDatabase.deviceDao().getDevice()
                val user = vpnDatabase.userDao().getUser()
                device?.let { vpnDatabase.deviceDao().delete(it) }
                user?.let { vpnDatabase.userDao().delete(it) }
            }
        }

        return signedOut
    }

    override suspend fun getLocations(): Result<List<Location>, String> {

        val apiResult = vpnApiService.getLocations().toResult().mapError { e -> e.message }

        var result = apiResult.fold(
            success = { newLocations ->
                Log.i(tag, "received ${newLocations.size} locations from API")
                val newLocationCodes = newLocations.map { it.code }
                vpnDatabase.locationDao().deleteNotIn(newLocationCodes);
                val dbLocations = newLocations.map { it.toDbLocation() }
                vpnDatabase.locationDao().insert(dbLocations)
                Ok(newLocations)
            },
            failure = { error ->
                Log.i(tag, "failed to get locations from API $error")
                Err(error)
            }
        )

        return result
    }

    override fun getRecentLocations(limit: Int): Flow<List<Location>> {
        return vpnDatabase.locationDao().recentLocations(limit)
            .map { it.map { l -> l.toModelLocation() } }
    }


    override suspend fun getUser(): User? {
        return vpnDatabase.userDao().getUser()
    }

}
