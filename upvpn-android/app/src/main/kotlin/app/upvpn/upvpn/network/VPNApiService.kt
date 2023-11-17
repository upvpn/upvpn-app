package app.upvpn.upvpn.network

import app.upvpn.upvpn.model.Accepted
import app.upvpn.upvpn.model.AddDeviceRequest
import app.upvpn.upvpn.model.AddDeviceResponse
import app.upvpn.upvpn.model.EndSessionApi
import app.upvpn.upvpn.model.Ended
import app.upvpn.upvpn.model.Location
import app.upvpn.upvpn.model.NewSession
import app.upvpn.upvpn.model.VpnSessionStatus
import app.upvpn.upvpn.model.VpnSessionStatusRequest
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VPNApiService {
    @POST("devices")
    suspend fun addDevice(@Body request: AddDeviceRequest): ApiResponse<AddDeviceResponse>

    @POST("sign-out")
    suspend fun signOut(): ApiResponse<Unit>

    @GET("locations")
    suspend fun getLocations(): ApiResponse<List<Location>>

    @POST("new-vpn-session")
    suspend fun newVpnSession(@Body request: NewSession): ApiResponse<Accepted>

    @POST("vpn-session-status")
    suspend fun getVpnSessionStatus(@Body request: VpnSessionStatusRequest): ApiResponse<VpnSessionStatus>

    @POST("end-vpn-session")
    suspend fun endVpnSession(@Body request: EndSessionApi): ApiResponse<Ended>
}
