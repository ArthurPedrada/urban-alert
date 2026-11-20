package com.example.feature.map

import com.example.core.config.PilotCityConfig
import com.example.core.model.IncidentStatus
import com.example.core.model.PublicAgency
import com.example.core.model.Severity
import com.example.data.incidents.IncidentDocument
import java.util.concurrent.TimeUnit

data class CategoryBreakdown(
    val slug: String,
    val title: String,
    val count: Int,
    val share: Float,
)

data class LocalAreaInsight(
    val localityTitle: String,
    val totalVisible: Int,
    val totalActive: Int,
    val urgentCount: Int,
    val recent24hCount: Int,
    val resolvedCount: Int,
    val topCategories: List<CategoryBreakdown>,
    val sortedRecent: List<IncidentDocument>,
    val latestIncident: IncidentDocument?,
    val relevantAgencies: List<PublicAgency>,
    val situationHeadline: String,
    val isUrgentSituation: Boolean,
)

object LocalAreaInsightCalculator {

    private val DAY_MILLIS = TimeUnit.HOURS.toMillis(24)

    fun compute(
        localityTitle: String,
        incidents: List<IncidentDocument>,
        nowMillis: Long = System.currentTimeMillis(),
    ): LocalAreaInsight {
        val activeIncidents = incidents.filter {
            val st = it.resolvedStatus()
            st != IncidentStatus.RESOLVED && st != IncidentStatus.REJECTED
        }
        val resolvedCount = incidents.count { it.resolvedStatus() == IncidentStatus.RESOLVED }
        val urgentIncidents = activeIncidents.filter {
            val sev = it.resolvedSeverity()
            sev == Severity.HIGH || sev == Severity.CRITICAL
        }
        val criticalCount = activeIncidents.count { it.resolvedSeverity() == Severity.CRITICAL }
        val recent24hCount = incidents.count { doc ->
            val createdMs = doc.createdAt?.toDate()?.time
            createdMs != null && (nowMillis - createdMs) in 0L..DAY_MILLIS
        }

        val sortedRecent = incidents.sortedByDescending { it.createdAt?.toDate()?.time ?: 0L }
        val latestIncident = sortedRecent.firstOrNull()

        val sourceForBreakdown = activeIncidents.ifEmpty { incidents }
        val denominator = sourceForBreakdown.size.coerceAtLeast(1).toFloat()
        val topCategories = sourceForBreakdown
            .groupBy { it.categorySlug }
            .map { (slug, list) ->
                CategoryBreakdown(
                    slug = slug,
                    title = list.first().categoryTitle(),
                    count = list.size,
                    share = (list.size / denominator).coerceIn(0f, 1f),
                )
            }
            .sortedByDescending { it.count }
            .take(4)

        val agencies = LinkedHashMap<String, PublicAgency>()
        val hasFloodingOrCritical = activeIncidents.any {
            it.categorySlug == "alagamentos" ||
                it.categorySlug == "problemas-de-drenagem" ||
                it.resolvedSeverity() == Severity.CRITICAL
        }
        if (hasFloodingOrCritical) {
            PilotCityConfig.findAgenciesForIncident(
                cityCode = PilotCityConfig.salvador.cityCode,
                categorySlug = "alagamentos",
            ).forEach { agencies[it.id] = it }
        }
        topCategories.forEach { cat ->
            PilotCityConfig.findAgenciesForIncident(
                cityCode = PilotCityConfig.salvador.cityCode,
                categorySlug = cat.slug,
            ).forEach { agencies[it.id] = it }
        }
        if (agencies.isEmpty()) {
            PilotCityConfig.salvadorAgencies.take(2).forEach { agencies[it.id] = it }
        }

        val dominant = topCategories.firstOrNull()
        val hasFloodingActive = activeIncidents.any { it.categorySlug == "alagamentos" }
        val headline = when {
            incidents.isEmpty() ->
                "Sem ocorrências registradas nesta janela. Mova o mapa ou toque em Reportar."
            criticalCount > 0 && hasFloodingActive ->
                "Alerta na região: $criticalCount registro(s) crítico(s) e ponto de alagamento ativo."
            urgentIncidents.isNotEmpty() ->
                "Atenção: ${urgentIncidents.size} ocorrência(s) urgente(s) · foco em ${dominant?.title ?: "infraestrutura"}."
            activeIncidents.isNotEmpty() && dominant != null ->
                "Principal demanda agora: ${dominant.title} (${dominant.count} ativa(s))."
            resolvedCount > 0 ->
                "Todas as $resolvedCount ocorrência(s) visíveis já constam como resolvidas."
            else ->
                "Monitoramento comunitário ativo nesta localidade."
        }

        return LocalAreaInsight(
            localityTitle = localityTitle,
            totalVisible = incidents.size,
            totalActive = activeIncidents.size,
            urgentCount = urgentIncidents.size,
            recent24hCount = recent24hCount,
            resolvedCount = resolvedCount,
            topCategories = topCategories,
            sortedRecent = sortedRecent,
            latestIncident = latestIncident,
            relevantAgencies = agencies.values.take(3),
            situationHeadline = headline,
            isUrgentSituation = urgentIncidents.isNotEmpty() || hasFloodingActive,
        )
    }
}
