package com.google.cloud.tools.gradle.endpoints.framework;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@WebServlet(
   initParams = {
     @WebInitParam(name = "services", value = "expectedApiClassBeta,expectedApiClassBeta2")
   }
)
class TestAnnotatedServletBeta {}
