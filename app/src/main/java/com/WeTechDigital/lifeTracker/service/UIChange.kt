package com.WeTechDigital.lifeTracker.service
//used for changing Ui of Main activity when motion detect
sealed class UIChange {
    object START : UIChange()
    object END : UIChange()
}