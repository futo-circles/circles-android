package org.futo.circles.di.data_source

import org.futo.circles.feature.notifications.*
import org.futo.circles.feature.notifications.test.task.*
import org.koin.dsl.module

val notificationsDsModule = module {
    factory { PushersManager(get(), get()) }
    single { NotificationUtils(get()) }
    factory { NotificationAccountSettingsTest(get()) }
    factory { NotificationAvailableUnifiedDistributorsTest(get(), get()) }
    factory { NotificationCurrentPushDistributorTest(get(), get()) }
    factory { NotificationPushRulesSettingsTest(get()) }
    factory { NotificationFromPushGatewayTest(get(), get()) }
    factory { NotificationSystemSettingsTest(get()) }
    factory { NotificationTestSend(get(), get()) }
    factory { PushHandler(get(), get(), get()) }
    single { NotificationDrawerManager(get(), get(), get(), get()) }
    factory { NotifiableEventResolver(get(), get(), get()) }
    factory { NotificationDisplayer(get()) }
    factory { NotifiableEventProcessor() }
    factory { NotificationRenderer(get(), get()) }
    factory { NotificationEventPersistence(get()) }
    factory { NotificationFactory(get(), get()) }
    factory { RoomGroupMessageCreator(get(), get(), get()) }
    factory { NotificationBitmapLoader(get()) }
    factory { RoomHistoryVisibilityFormatter(get()) }
    factory { RoleFormatter(get()) }
    factory { NoticeEventFormatter(get(), get(), get()) }
    factory { DisplayableEventFormatter(get(), get()) }
}