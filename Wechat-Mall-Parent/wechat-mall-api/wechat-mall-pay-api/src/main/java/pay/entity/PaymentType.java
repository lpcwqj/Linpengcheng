package pay.entity;


import com.lpc.entity.BaseEntity;
import lombok.Data;

import java.io.Serializable;

/**
 * 支付类型表
 */
@Data
public class PaymentType extends BaseEntity implements Serializable {

	/**
	 * 支付平台
	 */
	private String typeName;
	/**
	 * 同步通知
	 */
	private String frontUrl;

	/**
	 * 异步通知
	 */
	private String backUrl;
	/**
	 * 商户id
	 */
	private String merchantId;

}