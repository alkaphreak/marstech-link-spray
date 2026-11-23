package fr.marstech.mtlinkspray.domain.signature

/** Storage of [WriteSignature]s; the adapter expires them [WriteSignature.RETENTION] after creation. */
interface WriteSignatureStore {
    fun save(signature: WriteSignature)

    /** The 50 latest signatures whose resource id contains [query], newest first. */
    fun findLatestByResourceIdContaining(query: String): List<WriteSignature>
}
