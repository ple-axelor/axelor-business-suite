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
package com.axelor.apps.sale.service.saleorder.print;

import com.axelor.apps.base.db.Product;
import com.axelor.apps.sale.db.CustomerCatalog;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderLineRepository;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;

public class SaleOrderReportServiceImpl implements SaleOrderReportService {

  public static SaleOrderReportService getInstance() {
    return Beans.get(SaleOrderReportService.class);
  }

  // sale_order_type_select is in SupplyChain
  @Override
  public String getSaleOrderLineData(Long saleOrderId) {
    SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrderId);
    Map<String, Object> dataMap = new HashMap<>();
    List<SaleOrderLine> saleOrderLineList = saleOrder.getSaleOrderLineList();
    if (CollectionUtils.isNotEmpty(saleOrderLineList)) {
      for (SaleOrderLine saleOrderLine : saleOrderLineList) {
        dataMap.put("id", saleOrderLine.getId());
        dataMap.put("description", saleOrderLine.getDescription());
        dataMap.put("quantity", saleOrderLine.getQty());
        dataMap.put("ProductName", saleOrderLine.getProductName());
        dataMap.put("ex_tax_total", saleOrderLine.getExTaxTotal());
        dataMap.put("in_taxTotal", saleOrderLine.getInTaxTotal());
        dataMap.put("sequence", saleOrderLine.getSequence());
        dataMap.put("EstimatedDeliveryDate", saleOrderLine.getEstimatedDelivDate());
        dataMap.put("price_discounted", saleOrderLine.getPriceDiscounted());
        dataMap.put("showTotal", saleOrderLine.getIsShowTotal());
        dataMap.put("hideUnitAmounts", saleOrderLine.getIsHideUnitAmounts());

        Product product = saleOrderLine.getProduct();
        if (ObjectUtils.notEmpty(product)) {
          dataMap.put("productCode", product.getCode());
          dataMap.put("product_type_select", product.getProductTypeSelect());
          if (ObjectUtils.notEmpty(product.getPicture())) {
            dataMap.put("productPicture", product.getPicture().getFilePath());
          }
          if (CollectionUtils.isNotEmpty(product.getCustomerCatalogList())) {
            List<CustomerCatalog> customerCatalogList =
                product
                    .getCustomerCatalogList()
                    .stream()
                    .filter(
                        customerCatalog ->
                            customerCatalog
                                .getCustomerPartner()
                                .equals((saleOrder.getClientPartner())))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(customerCatalogList)) {
              for (CustomerCatalog customerCatalog : customerCatalogList) {
                dataMap.put("CustomerProductCode", customerCatalog.getProductCustomerCode());
                dataMap.put("CustomerProductName", customerCatalog.getProductCustomerName());
              }
            }
          }
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getUnit())) {
          dataMap.put("UnitCode", saleOrderLine.getUnit().getLabelToPrinting());
        }

        if (ObjectUtils.notEmpty(saleOrderLine.getTaxLine())) {
          dataMap.put("tax_line", saleOrderLine.getTaxLine().getValue());
        }

        BigDecimal unitPrice =
            saleOrder.getInAti() ? saleOrderLine.getInTaxPrice() : saleOrderLine.getPrice();
        dataMap.put("unit_price", unitPrice);

        BigDecimal totalDiscountAmount =
            saleOrderLine.getPriceDiscounted().subtract(unitPrice).multiply(saleOrderLine.getQty());
        dataMap.put("totalDiscountAmount", totalDiscountAmount);

        Boolean isTitleLine =
            saleOrderLine.getTypeSelect().equals(SaleOrderLineRepository.TYPE_TITLE);
        dataMap.put("is_title_line", isTitleLine);
        SaleOrderLine packHideUnitAmountsLine =
            Beans.get(SaleOrderLineRepository.class)
                .all()
                .filter(
                    "self.saleOrder = ?1 AND self.typeSelect = ?2 AND self.sequence > ?3 ORDER BY self.sequence",
                    saleOrder,
                    SaleOrderLineRepository.TYPE_TITLE,
                    saleOrderLine.getSequence())
                .fetchOne();
        if (ObjectUtils.notEmpty(packHideUnitAmountsLine)) {
          dataMap.put("PackHideUnitAmounts", packHideUnitAmountsLine.getIsHideUnitAmounts());
        }
      }
    }
    dataMap.put("CurrencyCode", saleOrder.getCurrency().getCode());
    dataMap.put("in_ati", saleOrder.getInAti());
    String dataMapJSONString = null;
    try {
      dataMapJSONString = new ObjectMapper().writeValueAsString(Arrays.asList(dataMap));
    } catch (JsonProcessingException e) {
      TraceBackService.trace(e);
    }
    return dataMapJSONString;
  }
}
