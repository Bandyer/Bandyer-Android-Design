/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.bottom_sheet.view

/**
 * Bandyer bottom sheet layout types supported
 */
sealed class BottomSheetLayoutType {

    /**
     * Grid layout
     */
    class GRID(val spanSize: Int): BottomSheetLayoutType()

    /**
     * List layout
     */
    class LIST: BottomSheetLayoutType()
}