package com.example.soundtoshare.dependency_injection

import com.example.soundtoshare.external.FireBaseDatabase
import com.example.soundtoshare.fragments.home.LikedSongsUseCase
import com.example.soundtoshare.fragments.home.VkGetDataUseCase
import com.example.soundtoshare.fragments.map.LikePlayUseCase
import com.example.soundtoshare.fragments.map.LocationUpdateUseCase
import com.example.soundtoshare.fragments.map.MoveCameraUseCase
import com.example.soundtoshare.fragments.map.UpdateMarkersUseCase
import com.example.soundtoshare.fragments.settings.IncognitoModeUseCase
import com.example.soundtoshare.repositories.LikedSongsRoomDB
import org.koin.dsl.module

val domainModule = module {
    factory<VkGetDataUseCase> {
        VkGetDataUseCase(vkApiRepository = get(), cacheRepository = get())
    }

    factory<IncognitoModeUseCase> {
        IncognitoModeUseCase(sharedPreferencesRepository = get(), cacheRepository = get())
    }
    factory<LikePlayUseCase>{
        LikePlayUseCase(roomDBRepository = get(), fireBaseDatabase = get())
    }
    factory<LikedSongsUseCase> {
        LikedSongsUseCase(roomDBRepository = get())
    }

    factory<LocationUpdateUseCase> {
        LocationUpdateUseCase(context = get(), userInfoRepository = get(), cacheRepository = get())
    }

    factory<UpdateMarkersUseCase> {
        UpdateMarkersUseCase(cacheRepository = get(), fireStoreDatabase = get(), context = get())
    }

    factory<MoveCameraUseCase>{
        MoveCameraUseCase()
    }
}