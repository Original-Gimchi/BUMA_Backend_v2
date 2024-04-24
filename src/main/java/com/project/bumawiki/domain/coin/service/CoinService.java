package com.project.bumawiki.domain.coin.service;

import static com.project.bumawiki.global.util.RandomUtil.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.bumawiki.domain.coin.domain.CoinAccount;
import com.project.bumawiki.domain.coin.domain.Price;
import com.project.bumawiki.domain.coin.domain.Trade;
import com.project.bumawiki.domain.coin.domain.TradeWithoutTradeStatusAndCoinAccountId;
import com.project.bumawiki.domain.coin.domain.type.TradeStatus;
import com.project.bumawiki.domain.coin.implementation.CoinAccountCreator;
import com.project.bumawiki.domain.coin.implementation.CoinAccountReader;
import com.project.bumawiki.domain.coin.implementation.PriceReader;
import com.project.bumawiki.domain.coin.implementation.TradeCreator;
import com.project.bumawiki.domain.coin.implementation.TradeReader;
import com.project.bumawiki.domain.coin.presentation.dto.RankingResponse;
import com.project.bumawiki.domain.user.domain.User;
import com.project.bumawiki.domain.user.implementation.UserReader;
import com.project.bumawiki.global.error.exception.BumawikiException;
import com.project.bumawiki.global.error.exception.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CoinService {
	public static final Long FIRST_MONEY = 10000000L;
	private final CoinAccountReader coinAccountReader;
	private final CoinAccountCreator coinAccountCreator;
	private final PriceReader priceReader;
	private final TradeReader tradeReader;
	private final TradeCreator tradeCreator;
	private final UserReader userReader;

	public CoinAccount createCoinAccount(User user) {
		boolean alreadyCreatedAccount = coinAccountReader.existsByUserId(user.getId());

		if (alreadyCreatedAccount) {
			throw new BumawikiException(ErrorCode.ALREADY_CREATED);
		}

		CoinAccount coinAccount = new CoinAccount(
			user.getId(),
			FIRST_MONEY
		);

		return coinAccountCreator.create(coinAccount);
	}

	@Transactional(readOnly = true)
	public CoinAccount findCoinAccountByUser(User currentUserWithLogin) {
		return coinAccountReader.getByUserId(currentUserWithLogin.getId());
	}

	public Trade buyCoin(TradeWithoutTradeStatusAndCoinAccountId coinData, User user) {
		CoinAccount coinAccount = coinAccountReader.getByUserId(user.getId());
		Price nowPrice = getRecentPrice();

		Trade trade = coinData.toTrade(coinAccount);

		if (coinData.coinPrice() >= nowPrice.getPrice()) {
			buyNow(trade, coinAccount);
		} else {
			buyLater(trade);
		}

		return tradeCreator.create(trade);
	}

	@Transactional
	public Trade sellCoin(TradeWithoutTradeStatusAndCoinAccountId coinData, User user) {
		CoinAccount coinAccount = coinAccountReader.getByUserId(user.getId());
		Price nowPrice = getRecentPrice();

		Trade trade = coinData.toTrade(coinAccount);

		if (coinData.coinPrice() <= nowPrice.getPrice()) {
			sellNow(trade, coinAccount);
		} else {
			sellLater(trade);
		}

		return tradeCreator.create(trade);
	}

	private void sellLater(Trade trade) {
		trade.updateTradeStatus(TradeStatus.SELLING);
	}

	private void sellNow(Trade trade, CoinAccount coinAccount) {
		coinAccount.sellCoin(trade.getCoinPrice(), trade.getCoinCount());
		trade.updateTradeStatus(TradeStatus.SOLD);
	}

	private void buyLater(Trade trade) {
		trade.updateTradeStatus(TradeStatus.BUYING);
	}

	private void buyNow(Trade trade, CoinAccount coinAccount) {
		coinAccount.buyCoin(trade.getCoinPrice(), trade.getCoinCount());
		trade.updateTradeStatus(TradeStatus.BOUGHT);
	}

	@Transactional(readOnly = true)
	public List<Trade> getTrades(Long accountId) {
		return tradeReader.findAllByCoinAccountIdOrderByTradedTimeDesc(accountId);
	}

	public List<Price> getPriceByPeriod(String period) {
		if (period.equals("full")) {
			return priceReader.findAllByOrderByStartedTime();
		}

		if (period.equals("halfMonth")) {
			LocalDateTime twoWeeksAgo = LocalDateTime.now().minusWeeks(2);
			return priceReader.findAllAfterStartedTime(twoWeeksAgo);
		}

		if (period.equals("week")) {
			LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
			return priceReader.findAllAfterStartedTime(oneWeekAgo);
		}

		if (period.equals("day")) {
			LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
			return priceReader.findAllAfterStartedTime(oneDayAgo);
		}

		if (period.equals("halfDay")) {
			LocalDateTime halfDayAgo = LocalDateTime.now().minusHours(12);
			return priceReader.findAllAfterStartedTime(halfDayAgo);
		}

		if (period.equals("threeHours")) {
			LocalDateTime threeHoursAgo = LocalDateTime.now().minusHours(3);
			return priceReader.findAllAfterStartedTime(threeHoursAgo);
		}

		throw new BumawikiException(ErrorCode.NO_PERIOD);
	}

	public void cancelTrade(Long tradeId, User user) {
		Trade trade = tradeReader.findById(tradeId);

		if (trade.getTradeStatus() != TradeStatus.BUYING && trade.getTradeStatus() != TradeStatus.SELLING) {
			throw new BumawikiException(ErrorCode.TRADE_ALREADY_FINISHED);
		}

		CoinAccount byId = coinAccountReader.getById(trade.getCoinAccountId());

		if (!byId.getUserId().equals(user.getId())) {
			throw new BumawikiException(ErrorCode.CANCEL_OTHERS_TRADE);
		}

		trade.updateTradeStatus(TradeStatus.CANCELLED);
	}

	public Long dailyCheck(User user) {
		long min = 50000;
		long max = 200000;

		SecureRandom random = getRandomInstance();
		Long randomNumber = (random.nextLong(max - min + 1) + min);
		randomNumber -= randomNumber % 10000;

		CoinAccount account = coinAccountReader.getByUserId(user.getId());

		if (account.wasRewardedToday()) {
			throw new BumawikiException(ErrorCode.ALREADY_AWARDED);
		}
		account.addMoney(randomNumber);
		account.updateLastRewardedTimeNow();

		return randomNumber;
	}

	public List<RankingResponse> getRanking(Pageable pageable) {
		Price recentPrice = getRecentPrice();
		return coinAccountReader.getRanking(pageable, recentPrice.getPrice())
			.stream()
			.map(ranking -> new RankingResponse(
					ranking,
					recentPrice.getPrice(),
					userReader.getById(ranking.getUserId())
				)
			).toList();
	}

	public Price getRecentPrice() {
		return priceReader.getRecentPrice();
	}
}
