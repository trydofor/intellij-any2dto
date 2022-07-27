// HI-MEEPO
// RNA:USE /com.moilioncircle.product/javaPackageName/
package com.moilioncircle.product;

import lombok.Data;
// RNA:EACH map/1/javaTypeImports/impt
// RNA:USE /java.time.LocalDateTime/impt/
import java.time.LocalDateTime;
// RNA:DONE impt

// RNA:USE /OuterPojo/className/
@Data
public class OuterPojo {
    // RNA:EACH map/1/javaFields/col
    // RNA:USE /String/col.type/
    // RNA:USE /userId/col.name/
    private String userId;
    // RNA:DONE col
}
