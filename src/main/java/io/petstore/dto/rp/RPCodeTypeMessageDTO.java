
package io.petstore.dto.rp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RPCodeTypeMessageDTO {
    private Long code;
    private String message; //id
    private String type;
}