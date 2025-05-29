package nicholos.tyler.philliesupdater

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class BaseballViewModel : ViewModel(){
    
    private val _baseballScheduleData = MutableStateFlow<GameRoot?>(null)
    val baseballScheduleData: MutableStateFlow<GameRoot?> = _baseballScheduleData
    
    private val _baseballGameData = MutableStateFlow<GameDetailResponse?>(null)
    val baseballGameData: MutableStateFlow<GameDetailResponse?> = _baseballGameData

    private val _selectedGame = MutableStateFlow<Game?>(null)
    val selectedGame: StateFlow<Game?> = _selectedGame

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: MutableStateFlow<String?> = _errorMessage
    
    init {
        Log.d("BaseballViewModel", "ViewModel initialized")
    }

    fun setSelectedGame(game: Game) {
        if (game != null) {
            _selectedGame.value = game
            _selectedGame.value!!.gamePk?.let { fetchGameDetails(it.toLong()) }
        }
    }
    
    fun fetchBaseballSchedule() {
        Log.i("BaseballViewModel", "FETCH_SCHEDULE_DATA_CALLED. ViewModel HashCode: ${this.hashCode()}")

        // Log the state of the viewModelScope's job BEFORE launching
        Log.d("BaseballViewModel", "viewModelScope.isActive: ${viewModelScope.coroutineContext.job.isActive}")
        Log.d("BaseballViewModel", "viewModelScope.isCancelled: ${viewModelScope.coroutineContext.job.isCancelled}")

        val job = viewModelScope.launch {
            // THIS IS THE VERY FIRST LINE INSIDE THE COROUTINE
            Log.i("BaseballViewModel", "COROUTINE_STARTED. ViewModel HashCode: ${this.hashCode()}")
            _errorMessage.value = null
            _baseballScheduleData.value = null // Clear previous data while loading

            try {
                Log.d("BaseballViewModel", "TRY_BLOCK_ENTERED. Calling API...")
                // Replace with your actual RetrofitClient setup if it's different
                val response = RetrofitClient().baseballApiService.getMlbSchedule(
                    sportId = 1,
                    startDate = "2025-05-25",
                    endDate = "2025-06-28",
                    teamId = 143
                )

                Log.d("BaseballViewModel", "API_RESPONSE_RECEIVED. Code: ${response.code()}, Successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val scheduleData = response.body()
                    if (scheduleData != null) {
                        Log.i("BaseballViewModel", "API_SUCCESS. Data: $baseballScheduleData")
                        _baseballScheduleData.value = scheduleData
                    } else {
                        Log.e("BaseballViewModel", "API_SUCCESS_BUT_NULL_BODY. Code: ${response.code()}")
                        _errorMessage.value = "Error: Empty response from server."
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("BaseballViewModel", "API_ERROR. Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: CancellationException) {
                Log.w("BaseballViewModel", "CATCH_BLOCK_CANCELLATION_EXCEPTION. ViewModel HashCode: ${this.hashCode()}", e)
                _errorMessage.value = "Request was cancelled."
                throw e // Re-throw cancellation exceptions as per best practice
            } catch (e: Exception) {
                Log.e("BaseballViewModel", "CATCH_BLOCK_EXCEPTION. ViewModel HashCode: ${this.hashCode()}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                Log.i("BaseballViewModel", "FINALLY_BLOCK_EXECUTED. ViewModel HashCode: ${this.hashCode()}")
            }
        }
        // Log job status immediately after launch
        Log.d("BaseballViewModel", "Job isActive: ${job.isActive}, isCancelled: ${job.isCancelled}, isCompleted: ${job.isCompleted}")
        Log.d("BaseballViewModel", "FETCH_CURRENT_WEATHER_ENDED_SYNC_PART. ViewModel HashCode: ${this.hashCode()}")
    }

    fun fetchGameDetails(gameId: Long) {
        Log.i("BaseballViewModel", "FETCH_SCHEDULE_DATA_CALLED. ViewModel HashCode: ${this.hashCode()}")

        // Log the state of the viewModelScope's job BEFORE launching
        Log.d("BaseballViewModel", "viewModelScope.isActive: ${viewModelScope.coroutineContext.job.isActive}")
        Log.d("BaseballViewModel", "viewModelScope.isCancelled: ${viewModelScope.coroutineContext.job.isCancelled}")

        val job = viewModelScope.launch {
            // THIS IS THE VERY FIRST LINE INSIDE THE COROUTINE
            Log.i("BaseballViewModel", "COROUTINE_STARTED. ViewModel HashCode: ${this.hashCode()}")
            _errorMessage.value = null
            _baseballGameData.value = null // Clear previous data while loading

            try {
                Log.d("BaseballViewModel", "TRY_BLOCK_ENTERED. Calling API...")
                // Replace with your actual RetrofitClient setup if it's different
                val response = RetrofitClient().baseballApiService.getGameDetails(
                    gamePk = gameId
                )

                Log.d("BaseballViewModel", "API_RESPONSE_RECEIVED. Code: ${response.code()}, Successful: ${response.isSuccessful}")

                if (response.isSuccessful) {
                    val gameData = response.body()
                    if (gameData != null) {
                        Log.i("BaseballViewModel", "API_SUCCESS. Data: $baseballGameData")
                        _baseballGameData.value = gameData
                    } else {
                        Log.e("BaseballViewModel", "API_SUCCESS_BUT_NULL_BODY. Code: ${response.code()}")
                        _errorMessage.value = "Error: Empty response from server."
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    Log.e("BaseballViewModel", "API_ERROR. Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                    _errorMessage.value = "Error: ${response.code()} - ${response.message()}"
                }
            } catch (e: CancellationException) {
                Log.w("BaseballViewModel", "CATCH_BLOCK_CANCELLATION_EXCEPTION. ViewModel HashCode: ${this.hashCode()}", e)
                _errorMessage.value = "Request was cancelled."
                throw e // Re-throw cancellation exceptions as per best practice
            } catch (e: Exception) {
                Log.e("BaseballViewModel", "CATCH_BLOCK_EXCEPTION. ViewModel HashCode: ${this.hashCode()}", e)
                _errorMessage.value = "Network error: ${e.message}"
            } finally {
                Log.i("BaseballViewModel", "FINALLY_BLOCK_EXECUTED. ViewModel HashCode: ${this.hashCode()}")
            }
        }
        // Log job status immediately after launch
        Log.d("BaseballViewModel", "Job isActive: ${job.isActive}, isCancelled: ${job.isCancelled}, isCompleted: ${job.isCompleted}")
        Log.d("BaseballViewModel", "FETCH_CURRENT_WEATHER_ENDED_SYNC_PART. ViewModel HashCode: ${this.hashCode()}")
    }
}