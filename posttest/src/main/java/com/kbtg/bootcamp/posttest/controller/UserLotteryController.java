package com.kbtg.bootcamp.posttest.controller;

import com.kbtg.bootcamp.posttest.entity.LotteryEntity;
import com.kbtg.bootcamp.posttest.entity.UserTicketEntity;
import com.kbtg.bootcamp.posttest.request.LotteryRequest;
import com.kbtg.bootcamp.posttest.service.impl.ImpLotteryService;
import com.kbtg.bootcamp.posttest.service.impl.ImpUserTicketService;
import jdk.jfr.Description;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
public class UserLotteryController {

    private final ImpLotteryService impLotteryService;
    private final ImpUserTicketService impUserTicketService;


    public UserLotteryController(ImpLotteryService impLotteryService, ImpUserTicketService impUserTicketService) {
        this.impLotteryService = impLotteryService;
        this.impUserTicketService = impUserTicketService;
    }


    @Description("USE BY USER FOR GET ALL LOTTERY THAT STILL REMAIN IN STORE")
    @GetMapping("/users/lotteries")
    @Validated
    public String getRemainLotteryFromStore() {
        String message = "";
        try {
            List<LotteryEntity> lotteryRemain = impLotteryService.getRemainLotteryFromStore();

            String listTicket = "\"tickets\": [";
            String tmp = "";
            if (lotteryRemain != null) {
                for (LotteryEntity lotteryEntity : lotteryRemain) {
                    tmp = tmp + "\"" + lotteryEntity.getTicket() + "\",";
                }
                message = listTicket + tmp.substring(0, tmp.length() - 1) + "]";
            }
        } catch (Exception e) {
            message = "don't have lottery in store";
        }
        return message;
    }


    @Description("USE BY USER FOR GET ALL LOTTERY THAT ALREADY BOUGHT ")
    @GetMapping("/users/lotteries/{id}")
    public String getAllOwnLotteryFromUser(@PathVariable("id") String user_Id) {
        String msg = "";
        int count = 0;
        int cost = 0;

        List<UserTicketEntity> ownLottery = impUserTicketService.getAllOwnLotteryFromUser(user_Id);

        try {

            String listTicket = "\"tickets\": [";
            String tmp = "";
            if (ownLottery != null) {
                for (UserTicketEntity userTicketEntity : ownLottery) {
                    tmp = tmp + "\"" + userTicketEntity.getTicket() + "\",";
                    count = count + 1;
                    cost = cost + (userTicketEntity.getAmount() * userTicketEntity.getPrice());
                }
                msg = listTicket + tmp.substring(0, tmp.length() - 1) + "], count = " + count + ", cost = " + cost;
            }
        } catch (Exception e) {
            msg = "don't have lottery in my pocket";
        }
        return msg;
    }

    @Description("USE BY USER FOR BUY LOTTERY FROM STORE")
    @PostMapping("/users/{userid}/lotteries/{ticket}")
    public String buyLotteryFromStore(@PathVariable String userid, @PathVariable String ticket) {
        boolean status = true;
        impLotteryService.updateStatusLottery(ticket, status);

        // get lottery details
        List<LotteryEntity> lotteryEntity = impLotteryService.getLotteryEntity(ticket);
        LotteryEntity lotteryEnt = lotteryEntity.get(0);

        // set TicketEntity
        UserTicketEntity userTicketEntity = new UserTicketEntity();
        userTicketEntity.setUserid(userid);
        userTicketEntity.setTicket(lotteryEnt.getTicket());
        userTicketEntity.setPrice(lotteryEnt.getPrice());
        userTicketEntity.setAmount(lotteryEnt.getAmount());
        UserTicketEntity addUserTicket = impUserTicketService.buyLotteryFromStore(userTicketEntity);

        //todo ผมไม่เเน่ใจว่าต้องให้เเสดงเป็นหมายเลขลอตเตอรี่หรือ id ของ record ใน database
        // return  "\"ticket\": " + "\"" + userTicketEntity.getTicket() + "\"";
        return "\"id\": " + "\"" + userTicketEntity.getId() + "\"";
    }


    @Description("USE BY USER FOR REFUND LOTTERY TO STORE")
    @DeleteMapping("/users/{userid}/lotteries/{ticket}")
    public String refundLotteryToStore(@PathVariable String userid, @PathVariable String ticket) {

        // Remove record from user_ticket table
        impUserTicketService.refundLotteryToStore(userid, ticket);

        // Update status to false in lottery table
        boolean status = false;
        impLotteryService.updateStatusLottery(ticket, status);

        return "\"ticket\": " + "\"" + ticket + "\"";

    }




}
