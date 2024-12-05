package ch.admin.foitt.wallet.util

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Constraints
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat

fun InputDescriptor.Companion.create(field: Field) = create(listOf(field))
fun InputDescriptor.Companion.create(fields: List<Field>) =
    InputDescriptor(
        constraints = Constraints(fields),
        formats = listOf(
            InputDescriptorFormat.VcSdJwt(
                sdJwtAlgorithms = listOf(SigningAlgorithm.ES512),
                kbJwtAlgorithms = listOf(),
            )
        ),
        id = "id",
        name = "name",
        purpose = "purpose"
    )

fun InputDescriptor.Companion.createFieldPerPath(jsonPaths: List<String>) =
    create(
        fields = jsonPaths.map { path ->
            Field(path = listOf(path))
        }
    )
