package depends.entity

/**
 * for kotlin jvm entities, there maybe a @JvmName("xxx")
 * annotation that modifies name of the entity
 */
interface IKotlinJvmEntity {
    var jvmName: GenericName
}