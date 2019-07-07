/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.ui

import org.projectforge.favorites.Favorites
import org.projectforge.framework.i18n.addTranslations
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.api.BaseDao
import org.projectforge.framework.persistence.api.BaseSearchFilter
import org.projectforge.framework.persistence.api.ExtendedBaseDO
import org.projectforge.framework.persistence.api.HibernateUtils
import org.projectforge.rest.core.AbstractBaseRest

/**
 * Utils for the Layout classes for handling auto max-length (get from JPA entities) and translations as well as
 * generic default layouts for list and edit pages.
 */
class LayoutUtils {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(LayoutUtils::class.java)

        fun addCommonTranslations(translations: MutableMap<String, String>) {
            addTranslations("select.placeholder", "calendar.today", "task.title.list.select", translations = translations)
        }

        /**
         * Auto-detects max-length of input fields (by referring the @Column annotations of clazz) and
         * i18n-keys (by referring the [org.projectforge.common.anots.PropertyInfo] annotations of clazz).
         * @return List of all elements used in the layout.
         */
        fun process(layout: UILayout): List<Any?> {
            val elements = processAllElements(layout.getAllElements())
            var counter = 0
            layout.namedContainers.forEach {
                it.key = "nc-${++counter}"
            }
            return elements
        }

        /**
         * Auto-detects max-length of input fields (by referring the @Column annotations of clazz) and
         * i18n-keys (by referring the [org.projectforge.common.anots.PropertyInfo] annotations of clazz).
         * <br>
         * If no named container called "filter-options" is found, it will be attached automatically by calling [addListFilterContainer]
         */
        fun processListPage(layout: UILayout): UILayout {
            var found = false
            layout.namedContainers.forEach {
                if (it.id == "filterOptions") {
                    found = true // Container found. Don't attach it automatically.
                }
            }
            if (!found) {
                addListFilterContainer(layout)
            }
            layout
                    .addAction(UIButton("reset", style = UIStyle.DANGER))
                    .addAction(UIButton("search", style = UIStyle.PRIMARY, default = true))
            process(layout)
            addCommonTranslations(layout)
            Favorites.addTranslations(layout.translations)
            return layout
        }

        fun addListFilterContainer(layout: UILayout, vararg elements: Any, filterClass: Class<*>? = null, autoAppendDefaultSettings: Boolean? = true) {
            val filterGroup = UIGroup()
            elements.forEach {
                when (it) {
                    is UIElement -> {
                        filterGroup.add(it)
                        if (filterClass != null && it is UILabelledElement) {
                            val property = getId(it)
                            if (property != null) {
                                val elementInfo = ElementsRegistry.getElementInfo(filterClass, property)
                                it.label = elementInfo?.i18nKey
                            }
                        }
                    }
                    is String -> {
                        val element = buildLabelInputElement(LayoutContext(filterClass), it)
                        if (element != null)
                            filterGroup.add(element)
                    }
                    else -> log.error("Element of type '${it::class.java}' not supported as child of filterContainer.")
                }
            }
            if (autoAppendDefaultSettings == true)
                addListDefaultOptions(filterGroup)
            layout.add(UINamedContainer("filterOptions").add(filterGroup))
        }

        /**
         * Adds the both checkboxes "only deleted" and "search history" to the given group. This is automatically called
         * by processListPage and appended to the first group found in named container "filter-options".
         */
        fun addListDefaultOptions(group: UIGroup) {
            group
                    .add(UICheckbox("deleted", label = "onlyDeleted", tooltip = "onlyDeleted.tooltip"))
                    .add(UICheckbox("searchHistory", label = "search.searchHistory", tooltip = "search.searchHistory.additional.tooltip"))
        }

        /**
         * Auto-detects max-length of input fields (by referring the @Column annotations of clazz) and
         * i18n-keys (by referring the @PropertColumn annotations of clazz).<br>
         * Adds the action buttons (cancel, undelete, markAsDeleted, update and/or add dependent on the given data.<br>
         * Calls also fun [process].
         * @see LayoutUtils.process
         */
        fun processEditPage(layout: UILayout, dto: Any,
                            restService: AbstractBaseRest<out ExtendedBaseDO<Int>, *, out BaseDao<*>, out BaseSearchFilter>)
                : UILayout {
            layout.addAction(UIButton("cancel", style = UIStyle.DANGER))
            if (restService.isHistorizable()) {
                // 99% of the objects are historizable (undeletable):
                if (restService.getId(dto) != null) {
                    if (restService.isDeleted(dto))
                        layout.addAction(UIButton("undelete", style = UIStyle.WARNING))
                    else
                        layout.addAction(UIButton("markAsDeleted", style = UIStyle.WARNING))
                }
            } else {
                // MemoDO for example isn't historizable:
                layout.addAction(UIButton("deleteIt", style = UIStyle.WARNING))
            }
            if (restService.getId(dto) != null) {
                if (restService.cloneSupported) {
                    layout.addAction(UIButton("clone", style = UIStyle.SECONDARY))
                }
                if (!restService.isDeleted(dto))
                    layout.addAction(UIButton("update", style = UIStyle.PRIMARY, default = true))
            } else {
                layout.addAction(UIButton("create", style = UIStyle.PRIMARY, default = true))
            }
            process(layout)
            layout.addTranslations("label.historyOfChanges")
            addCommonTranslations(layout)
            return layout
        }

        private fun addCommonTranslations(layout: UILayout) {
            addCommonTranslations(layout.translations)
        }

        /**
         * @param layoutSettings One element is returned including the label (e. g. UIInput).
         */
        internal fun buildLabelInputElement(layoutSettings: LayoutContext, id: String): UIElement? {
            return ElementsRegistry.buildElement(layoutSettings, id)
        }

        /**
         * @param createRowCol If true, a new [UIRow] containing a new [UICol] with the given element is returned,
         * otherwise the element itself without any other operation.
         * @return The element itself or the surrounding [UIRow].
         */
        internal fun prepareElementToAdd(element: UIElement, createRowCol: Boolean): UIElement {
            return if (createRowCol) {
                val row = UIRow()
                val col = UICol()
                row.add(col)
                col.add(element)
                row
            } else {
                element
            }
        }

        internal fun setLabels(elementInfo: ElementsRegistry.ElementInfo?, element: UILabelledElement) {
            if (elementInfo == null)
                return
            if (!elementInfo.i18nKey.isNullOrEmpty())
                element.label = elementInfo.i18nKey
            if (!elementInfo.additionalI18nKey.isNullOrEmpty() && element.ignoreAdditionalLabel != true)
                element.additionalLabel = elementInfo.additionalI18nKey
        }

        /**
         * Does translation of buttons and UILabels
         * @param elements List of all elements used in the layout.
         * @return The unmodified parameter elements.
         * @see HibernateUtils.getPropertyLength
         */
        private fun processAllElements(elements: List<Any>): List<Any?> {
            var counter = 0
            elements.forEach {
                if (it is UIElement) it.key = "el-${++counter}"
                when (it) {
                    is UILabelledElement -> {
                        it.label = getLabelTransformation(it.label, it as UIElement)
                        it.additionalLabel = getLabelTransformation(it.additionalLabel, it, additionalLabel = true)
                        it.tooltip = getLabelTransformation(it.tooltip)
                    }
                    is UIFieldset -> {
                        it.title = getLabelTransformation(it.title, it as UIElement)
                    }
                    is UITableColumn -> {
                        val translation = getLabelTransformation(it.title)
                        if (translation != null) it.title = translation
                    }
                    is UIButton -> {
                        if (it.title == null) {
                            val i18nKey = when (it.id) {
                                "cancel" -> "cancel"
                                "clone" -> "clone"
                                "create" -> "create"
                                "deleteIt" -> "delete"
                                "markAsDeleted" -> "markAsDeleted"
                                "reset" -> "reset"
                                "search" -> "search"
                                "undelete" -> "undelete"
                                "update" -> "save"
                                else -> null
                            }
                            if (i18nKey == null) {
                                log.error("i18nKey not found for action button '${it.id}'.")
                            } else {
                                it.title = translate(i18nKey)
                            }
                        }
                    }
                }
            }
            return elements
        }

        /**
         * @return The id of the given element if supported.
         */
        internal fun getId(element: UIElement?, followLabelReference: Boolean = true): String? {
            if (element == null) return null
            if (followLabelReference && element is UILabel) {
                return getId(element.reference)
            }
            return when (element) {
                is UIInput -> element.id
                is UICheckbox -> element.id
                is UISelect<*> -> element.id
                is UITextArea -> element.id
                is UITableColumn -> element.id
                else -> null
            }
        }

        /**
         * If the given label starts with "'" the label itself as substring after "'" will be returned: "'This is an text." -> "This is an text"<br>
         * Otherwise method [translate] will be called and the result returned.
         * @param label to process
         * @return Modified label or unmodified label.
         */
        internal fun getLabelTransformation(label: String?, labelledElement: UIElement? = null, additionalLabel: Boolean = false): String? {
            if (label == null) {
                if (labelledElement is UILabelledElement) {
                    if (additionalLabel && labelledElement.ignoreAdditionalLabel) {
                        return null
                    }
                    val layoutSettings = labelledElement.layoutContext
                    if (layoutSettings != null) {
                        val id = getId(labelledElement)
                        if (id != null) {
                            var elementInfo = ElementsRegistry.getElementInfo(layoutSettings, id)
                            if (!additionalLabel && elementInfo?.i18nKey != null) {
                                return translate(elementInfo.i18nKey)
                            }
                            if (additionalLabel && elementInfo?.additionalI18nKey != null) {
                                return translate(elementInfo.additionalI18nKey)
                            }
                        }
                    }
                }
                return null
            }
            if (label.startsWith("'"))
                return label.substring(1)
            return translate(label)
        }

    }
}
