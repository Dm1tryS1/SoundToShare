package com.example.soundtoshare.fragments.home

import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.room.Room
import com.example.soundtoshare.repositories.LikedSongsRoomDB
import com.example.soundtoshare.repositories.Reaction
import com.example.soundtoshare.repositories.UserInfo
import com.example.soundtoshare.repositories.roomdb.LikedSong
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKScope
import com.vk.sdk.api.audio.dto.AudioAudio
import java.util.*
import java.util.concurrent.TimeUnit.*

class HomeViewModel(val vkGetDataUseCase : VkGetDataUseCase, val likedSongsUseCase: LikedSongsUseCase) : ViewModel() {
    private val firebaseGetDataUseCase = FireBaseGetDataUseCase()
    private val reactions : MutableLiveData<MutableList<Reaction>> by lazy {
        MutableLiveData<MutableList<Reaction>>()
    }
    private val likedSongs : MutableLiveData<MutableList<LikedSong>> by lazy {
        MutableLiveData<MutableList<LikedSong>>()
    }

    init{
        Log.d("ViewModel", "Created ViewModel")
        reactions.postValue(mutableListOf())
    }

    fun signInVK(
        authLauncher: ActivityResultLauncher<Collection<VKScope>>,
        arrayList: ArrayList<VKScope>
    ) {
        authLauncher.launch(arrayList)
    }

    fun loadLikedSongs(){
        likedSongsUseCase.getLikedSongs { likedSongs.postValue(this) }
    }

    fun loadUserInfo() {
        Log.d("test","KoinViewModel")
        vkGetDataUseCase.loadUserInfo(){}
        firebaseGetDataUseCase.getReactions(VK.getUserId().toString()) {
            reactions.value?.add(this) ?: Log.d("firebase", "cannot add item")
            reactions.postValue(reactions.value)
            reactions.value?.forEach { Log.d("firebase", it.toString()) } ?: Log.d(
                "firebase",
                "ya hz"
            )
        }

    }

    fun getObservableReactions(): MutableLiveData<MutableList<Reaction>> {
        return reactions
    }

    fun getObservableLikedSongs(): MutableLiveData<MutableList<LikedSong>> {
        return likedSongs
    }

    fun fetchVkMusicViewModel(fetchVkMusicCallback: () -> Unit) {
        vkGetDataUseCase.fetchVkMusic {
            fetchVkMusicCallback()
        }
    }

    fun getUserInfoLiveData(): MutableLiveData<UserInfo> {
        return vkGetDataUseCase.getUserInfoLiveData()
    }

    fun addLikedSong() {
        likedSongsUseCase.addLikedSong()
    }
}
