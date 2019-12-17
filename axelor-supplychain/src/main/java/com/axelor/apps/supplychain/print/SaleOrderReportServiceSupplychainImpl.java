/*
 * Axelor Business Solutions
 *
 * Copyright (C) 2019 Axelor (<http://axelor.com>).
 *
 * This program is free software: you can redistribute it and/or  modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.axelor.apps.supplychain.print;

import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.print.SaleOrderReportServiceImpl;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleOrderReportServiceSupplychainImpl extends SaleOrderReportServiceImpl {

  @Override
  public List<Map<String, Object>> getSaleOrderLineData(Long saleOrderId) {
    List<Map<String, Object>> dataMapList = super.getSaleOrderLineData(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    dataMap.put("sale_order_type_select", saleOrder.getSaleOrderTypeSelect());
    dataMapList.add(dataMap);
    return dataMapList;
  }

  @Override
  public List<Map<String, Object>> getSaleOrderLineTaxData(Long saleOrderId) {
    List<Map<String, Object>> dataMapList = super.getSaleOrderLineTaxData(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    dataMap.put("sale_order_type_select", saleOrder.getSaleOrderTypeSelect());
    dataMapList.add(dataMap);
    return dataMapList;
  }

  @Override
  public List<Map<String, Object>> getSaleOrderData(Long saleOrderId) {
    List<Map<String, Object>> dataMapList = super.getSaleOrderData(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    dataMap.put("sale_order_type_select", saleOrder.getSaleOrderTypeSelect());
    dataMap.put("is_ispm_required", saleOrder.getIsIspmRequired());

    if (ObjectUtils.notEmpty(saleOrder.getShipmentDate())) {
      dataMap.put("ShipmentDate", DateTool.toDate(saleOrder.getShipmentDate()));
    }

    if (ObjectUtils.notEmpty(saleOrder.getPaymentCondition())) {
      dataMap.put("PaymentCondName", saleOrder.getPaymentCondition().getName());
    }
    if (ObjectUtils.notEmpty(saleOrder.getPaymentMode())) {
      dataMap.put("PaymentMode", saleOrder.getPaymentMode().getName());
    }
    dataMapList.add(dataMap);
    return dataMapList;
  }
}
