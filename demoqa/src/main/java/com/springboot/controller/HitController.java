package com.springboot.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.abtasty.flagship.hits.Event;
import com.abtasty.flagship.hits.Item;
import com.abtasty.flagship.hits.Page;
import com.abtasty.flagship.hits.Screen;
import com.abtasty.flagship.hits.Transaction;
import com.abtasty.flagship.visitor.Visitor;

import static com.springboot.controller.VisitorController.VisitorAttribute;

@RestController
public class HitController {

    @RequestMapping(method = RequestMethod.POST, value = "/hit")
    public String sendHit(HttpServletRequest request, @RequestBody Map env) {

        Screen screen;
        Page page;
        Event event;
        Item item;
        Transaction transaction;

//        Visitor visitor = (Visitor) request.getAttribute("HitVisitor");
        Visitor visitor = (Visitor) request.getSession().getAttribute(VisitorAttribute);

        switch (env.get("t").toString()) {

            case "SCREEN":
                screen = new Screen(env.get("dl").toString());

                if (env.get("re_wi") != null && !env.get("re_wi").toString().equals("") && env.get("re_he") != null && !env.get("re_he").toString().equals("")) {
                    screen.withResolution(Integer.parseInt(env.get("re_wi").toString()), Integer.parseInt(env.get("re_he").toString()));
                }

                if (env.get("ul") != null && !env.get("ul").toString().equals("")) {
                    screen.withLocale(env.get("ul").toString());
                }

                if (env.get("uip") != null && !env.get("uip").toString().equals("")) {
                    screen.withIp(env.get("uip").toString());
                }

                if (env.get("sn") != null && !env.get("sn").toString().equals("")) {
                    screen.withSessionNumber(Integer.parseInt(env.get("sn").toString()));
                }

                System.out.println("screen " + screen);
                visitor.sendHit(screen);
                break;

            case "PAGE":
                page = new Page(env.get("dl").toString());
                System.out.println("Map" + env);

                if (env.get("re_wi") != null && env.get("re_he") != null && !env.get("re_wi").toString().equals("") && !env.get("re_he").toString().equals("")) {
                    page.withResolution(Integer.parseInt(env.get("re_wi").toString()), Integer.parseInt(env.get("re_he").toString()));
                }

                if (env.get("ul") != null && !env.get("ul").toString().equals("")) {
                    page.withLocale(env.get("ul").toString());
                }

                if (env.get("uip") != null && !env.get("uip").toString().equals("")) {
                    page.withIp(env.get("uip").toString());
                }

                if (env.get("sn") != null && !env.get("sn").toString().equals("")) {
                    page.withSessionNumber(Integer.parseInt(env.get("sn").toString()));
                }

                System.out.println("page " + page);
                visitor.sendHit(page);
                break;

            case "EVENT":
                event = new Event(env.get("ec").toString().equals("ue") ? Event.EventCategory.USER_ENGAGEMENT : Event.EventCategory.ACTION_TRACKING, env.get("ea").toString());

                if (env.get("el") != null && !env.get("el").toString().equals("")) {
                    event.withEventLabel(env.get("el").toString());
                }

                if (env.get("ev") != null && !env.get("ev").toString().equals("")) {
                    event.withEventValue(Double.parseDouble(env.get("ev").toString()));
                }

                if (env.get("re_wi") != null && !env.get("re_wi").toString().equals("") && env.get("re_he") != null && !env.get("re_he").toString().equals("")) {
                    event.withResolution(Integer.parseInt(env.get("re_wi").toString()), Integer.parseInt(env.get("re_he").toString()));
                }

                if (env.get("ul") != null && !env.get("ul").toString().equals("")) {
                    event.withLocale(env.get("ul").toString());
                }

                if (env.get("uip") != null && !env.get("uip").toString().equals("")) {
                    event.withIp(env.get("uip").toString());
                }

                if (env.get("sn") != null && !env.get("sn").toString().equals("")) {
                    event.withSessionNumber(Integer.parseInt(env.get("sn").toString()));
                }

                System.out.println("event " + event);
                visitor.sendHit(event);
                break;

            case "ITEM":

                item = new Item(env.get("tid").toString(), env.get("in").toString(), env.get("ic").toString());

                if (env.get("iv") != null && !env.get("iv").toString().equals("")) {
                    item.withItemCategory(env.get("iv").toString());
                }

                if (env.get("ip") != null && !env.get("ip").toString().equals("")) {
                    item.withItemPrice(Float.parseFloat(env.get("ip").toString()));
                }

                if (env.get("iq") != null && !env.get("iq").toString().equals("")) {
                    item.withItemQuantity(Integer.parseInt(env.get("iq").toString()));
                }

                if (env.get("re_wi") != null && !env.get("re_wi").toString().equals("") && env.get("re_he") != null && !env.get("re_he").toString().equals("")) {
                    item.withResolution(Integer.parseInt(env.get("re_wi").toString()), Integer.parseInt(env.get("re_he").toString()));
                }

                if (env.get("ul") != null && !env.get("ul").toString().equals("")) {
                    item.withLocale(env.get("ul").toString());
                }

                if (env.get("uip") != null && !env.get("uip").toString().equals("")) {
                    item.withIp(env.get("uip").toString());
                }

                if (env.get("sn") != null && !env.get("sn").toString().equals("")) {
                    item.withSessionNumber(Integer.parseInt(env.get("sn").toString()));
                }

                System.out.println("item " + item);
                visitor.sendHit(item);
                break;

            case "TRANSACTION":
                transaction = new Transaction(env.get("tid").toString(), env.get("ta").toString());

                if (env.get("tcc") != null && !env.get("tcc").toString().equals("")) {
                    transaction.withCouponCode(env.get("tcc").toString());
                }

                if (env.get("sm") != null && !env.get("sm").toString().equals("")) {
                    transaction.withShippingMethod(env.get("sm").toString());
                }

                if (env.get("ts") != null && !env.get("ts").toString().equals("")) {
                    transaction.withShippingCosts(Float.parseFloat(env.get("ts").toString()));
                }

                if (env.get("tr") != null && !env.get("tr").toString().equals("")) {
                    transaction.withTotalRevenue(Float.parseFloat(env.get("tr").toString()));
                }

                if (env.get("tt") != null && !env.get("tt").toString().equals("")) {
                    transaction.withTaxes(Float.parseFloat(env.get("tt").toString()));
                }

                if (env.get("tc") != null && !env.get("tc").toString().equals("")) {
                    transaction.withCurrency(env.get("tc").toString());
                }

                if (env.get("pm") != null && !env.get("pm").toString().equals("")) {
                    transaction.withPaymentMethod(env.get("pm").toString());
                }

                if (env.get("icn") != null && !env.get("icn").toString().equals("")) {
                    transaction.withItemCount(Integer.parseInt(env.get("icn").toString()));
                }

                if (env.get("re_wi") != null && !env.get("re_wi").toString().equals("") && env.get("re_he") != null && !env.get("re_he").toString().equals("")) {
                    transaction.withResolution(Integer.parseInt(env.get("re_wi").toString()), Integer.parseInt(env.get("re_he").toString()));
                }

                if (env.get("ul") != null && !env.get("ul").toString().equals("")) {
                    transaction.withLocale(env.get("ul").toString());
                }

                if (env.get("uip") != null && !env.get("uip").toString().equals("")) {
                    transaction.withIp(env.get("uip").toString());
                }

                if (env.get("sn") != null && !env.get("sn").toString().equals("")) {
                    transaction.withSessionNumber(Integer.parseInt(env.get("sn").toString()));
                }

                System.out.println("transaction " + transaction);
                visitor.sendHit(transaction);
                break;

        }

        return "env :" + env;
    }
}
