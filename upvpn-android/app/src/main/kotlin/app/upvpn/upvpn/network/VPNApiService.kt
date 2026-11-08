package app.upvpn.upvpn.network

import app.upvpn.upvpn.model.Accepted
import app.upvpn.upvpn.model.AddDeviceRequest
import app.upvpn.upvpn.model.AddDeviceResponse
import app.upvpn.upvpn.model.SsoAddDeviceRequest
import app.upvpn.upvpn.model.EndSessionApi
import app.upvpn.upvpn.model.Ended
import app.upvpn.upvpn.model.LocationsV2
import app.upvpn.upvpn.model.NewSession
import app.upvpn.upvpn.model.OnlyEmail
import app.upvpn.upvpn.model.UserCredentialsWithCode
import app.upvpn.upvpn.model.UserPlan
import app.upvpn.upvpn.model.VpnSessionStatus
import app.upvpn.upvpn.model.VpnSessionStatusRequest
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface VPNApiService {
    @POST("v1/devices")
    suspend fun addDevice(@Body request: AddDeviceRequest): ApiResponse<AddDeviceResponse>

    @POST("v1/sso/devices")
    suspend fun ssoAddDevice(@Body request: SsoAddDeviceRequest): ApiResponse<AddDeviceResponse>

    @POST("v1/sign-out")
    suspend fun signOut(): ApiResponse<Unit>

    @POST("v1/account/send-code")
    suspend fun requestCode(@Body request: OnlyEmail): ApiResponse<Unit>

    @POST("v1/account")
    suspend fun signUp(@Body request: UserCredentialsWithCode): ApiResponse<Unit>

    @GET("v1/plan/current")
    suspend fun getUserPlan(): ApiResponse<UserPlan>

    @GET("v2/locations")
    suspend fun getLocations(): ApiResponse<LocationsV2>

    @POST("v1/new-vpn-session")
    suspend fun newVpnSession(@Body request: NewSession): ApiResponse<Accepted>

    @POST("v1/vpn-session-status")
    suspend fun getVpnSessionStatus(@Body request: VpnSessionStatusRequest): ApiResponse<VpnSessionStatus>

    @POST("v1/end-vpn-session")
    suspend fun endVpnSession(@Body request: EndSessionApi): ApiResponse<Ended>
}
