package com.stakater.nordmart.catalog.controller;

import com.stakater.nordmart.catalog.domain.Product;
import com.stakater.nordmart.catalog.repository.ProductRepository;
import com.stakater.nordmart.catalog.common.IstioHeaders;
import com.stakater.nordmart.catalog.common.Utils;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/api/products")
public class CatalogController {

    private static final Logger LOG = LoggerFactory.getLogger(CatalogController.class);

    @Autowired
    private ProductRepository repository;

    @ResponseBody
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Product> getAll() {
        IstioHeaders istioHeaders = new IstioHeaders(Utils.getCurrentHttpRequest());
        LOG.info(istioHeaders.toString());

        Spliterator<Product> products = repository.findAll().spliterator();
        return StreamSupport.stream(products, false).collect(Collectors.toList());
    }
}
